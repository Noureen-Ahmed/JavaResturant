package classes;

import java.util.Date;


public class Invoice {
    private int invoiceId;
    private Order order; 
    private double amount;
    private String paymentMethod;
    private Date paymentDate;
    private boolean paid;

    public Invoice() {
        this.paymentMethod = "CASH";
        this.paymentDate = new Date();
        this.paid = false;
    }

    public Invoice(double amount) {
        this(0, amount, "CASH", new Date());
    }

    public Invoice(double amount, String paymentMethod) {
        this(0, amount, paymentMethod, new Date());
    }

    public Invoice(int invoiceId, double amount) {
        this(invoiceId, amount, "CASH", new Date());
    }

    public Invoice(int invoiceId, double amount, String paymentMethod, Date paymentDate) {
        setInvoiceId(invoiceId);
        setAmount(amount);
        setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        setPaymentDate(paymentDate != null ? paymentDate : new Date());
        this.paid = false;
    }


    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        if (invoiceId < 0) {
            throw new IllegalArgumentException("Invoice ID cannot be negative.");
        }
        this.invoiceId = invoiceId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            this.amount = order.getTotalAmount();
        }
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty()) 
                ? paymentMethod : "CASH";
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate != null ? paymentDate : new Date();
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }


    public boolean processPayment() {
        if (this.paid) {
            return true;
        }
        this.paid = true;
        this.paymentDate = new Date();
        if (this.order != null) {
            this.order.setStatus(OrderStatus.COMPLETED);
        }
        return true;
    }

    public String printReceipt() {
        return String.format("--- INVOICE #%d ---\nAmount: $%.2f\nPayment Method: %s\nDate: %s\nStatus: %s\n",
                invoiceId, amount, paymentMethod != null ? paymentMethod : "N/A",
                paymentDate != null ? paymentDate.toString() : "N/A",
                paid ? "PAID" : "UNPAID");
    }
}