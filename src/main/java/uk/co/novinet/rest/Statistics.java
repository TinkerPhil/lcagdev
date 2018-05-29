package uk.co.novinet.rest;

public class Statistics {
    private Double totalContributions;
    private int totalContributors;
    private int numberOfRegisteredMembers;
    private int numberOfGuests;
    private int totalUsers;

    public Statistics(Double totalContributions, int totalContributors, int numberOfRegisteredMembers, int numberOfGuests, int totalUsers) {
        this.totalContributions = totalContributions;
        this.totalContributors = totalContributors;
        this.numberOfRegisteredMembers = numberOfRegisteredMembers;
        this.numberOfGuests = numberOfGuests;
        this.totalUsers = totalUsers;
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

    public int getTotalUsers() {
        return totalUsers;
    }
}
