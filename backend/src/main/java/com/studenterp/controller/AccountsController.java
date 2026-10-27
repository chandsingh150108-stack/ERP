package com.studenterp.controller;

import com.studenterp.entity.*;
import com.studenterp.exception.ResourceNotFoundException;
import com.studenterp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final FeeStructureRepository feeStructureRepository;
    private final InvoiceRepository invoiceRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMyAccount(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        List<Invoice> invoices = invoiceRepository.findByStudentIdOrderByDueDateAsc(studentId);

        BigDecimal totalDue = invoices.stream()
                .filter(i -> !"PAID".equals(i.getStatus()) && !"WAIVED".equals(i.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = invoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingCount = invoices.stream().filter(i -> "PENDING".equals(i.getStatus())).count();
        long overdueCount = invoices.stream().filter(i -> "OVERDUE".equals(i.getStatus())).count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalDue", totalDue);
        result.put("totalPaid", totalPaid);
        result.put("pendingInvoices", pendingCount);
        result.put("overdueInvoices", overdueCount);
        result.put("invoices", invoices);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me/invoices")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Invoice>> getMyInvoices(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        return ResponseEntity.ok(invoiceRepository.findByStudentIdOrderByDueDateAsc(studentId));
    }

    @GetMapping("/me/payments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<FeePayment>> getMyPayments(Authentication auth) {
        Long studentId = resolveStudentId(auth);
        return ResponseEntity.ok(feePaymentRepository.findByStudentIdOrderByPaymentDateDesc(studentId));
    }

    @GetMapping("/fee-structures")
    public ResponseEntity<List<FeeStructure>> getFeeStructures() {
        return ResponseEntity.ok(feeStructureRepository.findAll());
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalInvoices", invoiceRepository.count());
        report.put("paidCount", invoiceRepository.countByStatus("PAID"));
        report.put("pendingCount", invoiceRepository.countByStatus("PENDING"));
        report.put("overdueCount", invoiceRepository.countByStatus("OVERDUE"));

        List<Invoice> allInvoices = invoiceRepository.findAll();
        BigDecimal totalCollected = allInvoices.stream()
                .filter(i -> "PAID".equals(i.getStatus()))
                .map(Invoice::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = allInvoices.stream()
                .filter(i -> !"PAID".equals(i.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("totalCollected", totalCollected);
        report.put("totalPending", totalPending);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/fee-structures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructure> createFeeStructure(@RequestBody FeeStructure feeStructure) {
        return ResponseEntity.ok(feeStructureRepository.save(feeStructure));
    }

    @PostMapping("/generate-invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateInvoices(@RequestParam Long programId,
                                                                @RequestParam Long academicYearId) {
        FeeStructure feeStructure = feeStructureRepository
                .findByProgramIdAndAcademicYearIdAndFeeCategory(programId, academicYearId, "REGULAR")
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", "program", programId));

        List<Student> students = studentRepository.findAll();
        int created = 0;

        for (Student student : students) {
            if (student.getProgram() == null || !student.getProgram().getId().equals(programId)) continue;
            String invNum = "INV-" + LocalDate.now().getYear() + "-" +
                    String.format("%06d", System.nanoTime() % 1000000);

            Invoice invoice = Invoice.builder()
                    .student(student)
                    .feeStructure(feeStructure)
                    .invoiceNumber(invNum)
                    .amount(feeStructure.getTotalAmount())
                    .amountPaid(BigDecimal.ZERO)
                    .dueDate(feeStructure.getDueDate())
                    .status("PENDING")
                    .build();
            invoiceRepository.save(invoice);
            created++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("generated", created);
        result.put("message", "Generated " + created + " invoices");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeePayment> recordPayment(@RequestBody Map<String, Object> payload) {
        Long invoiceId = Long.valueOf(payload.get("invoiceId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String mode = (String) payload.getOrDefault("paymentMode", "CASH");
        String txnId = (String) payload.getOrDefault("transactionId", "");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        FeePayment payment = FeePayment.builder()
                .invoice(invoice)
                .student(invoice.getStudent())
                .amount(amount)
                .paymentMode(mode)
                .transactionId(txnId)
                .paymentDate(LocalDate.now())
                .status("SUCCESS")
                .build();
        feePaymentRepository.save(payment);

        // Update invoice
        invoice.setAmountPaid(invoice.getAmountPaid().add(amount));
        invoice.setStatus(invoice.getAmountPaid().compareTo(invoice.getAmount()) >= 0 ? "PAID" : "PARTIAL");
        if ("PAID".equals(invoice.getStatus())) invoice.setPaidDate(LocalDate.now());
        invoiceRepository.save(invoice);

        return ResponseEntity.ok(payment);
    }

    @PutMapping("/invoices/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invoice> waiveInvoice(@PathVariable Long id,
                                                  @RequestParam(required = false) String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        invoice.setStatus("WAIVED");
        invoice.setRemarks(reason);
        return ResponseEntity.ok(invoiceRepository.save(invoice));
    }

    private Long resolveStudentId(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "user", username))
                .getId();
    }
}
