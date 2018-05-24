package uk.co.novinet.rest;

public class Statistics {
    private Double totalContributions;
    private int totalContributors;
    private int numberOfRegisteredMembers;
    private int numberOfGuests;

    public Statistics(Double totalContributions, int totalContributors, int numberOfRegisteredMembers, int numberOfGuests) {
        this.totalContributions = totalContributions;
        this.totalContributors = totalContributors;
        this.numberOfRegisteredMembers = numberOfRegisteredMembers;
        this.numberOfGuests = numberOfGuests;
    }

    public Double getTotalContributions() {
        return totalContributions;
    }

    public int getTotalContributors() {
        return totalContributors;
    }

    public int getNumberOfRegisteredMembers() {
        return numberOfRegisteredMembers;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }
}
