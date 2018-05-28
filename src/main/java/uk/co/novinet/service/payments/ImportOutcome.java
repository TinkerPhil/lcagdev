package uk.co.novinet.service.payments;

public class ImportOutcome {
    private int importedTransactions;
    private int totalTransactions;

    public ImportOutcome(int importedTransactions, int totalTransactions) {
        this.importedTransactions = importedTransactions;
        this.totalTransactions = totalTransactions;
    }

    public int getImportedTransactions() {
        return importedTransactions;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    @Override
    public String toString() {
        return "ImportOutcome{" +
                "importedTransactions=" + importedTransactions +
                ", totalTransactions=" + totalTransactions +
                '}';
    }
}
