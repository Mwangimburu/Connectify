package com.example.connectify.models;

public class Member {
    String memberTo;
    private String agentId;

    public Member() {
    }

    public Member(String memberTo, String agentId) {
        this.memberTo = memberTo;
        this.agentId= agentId;
    }

    public String getMemberTo() {
        return memberTo;
    }

    public void setMemberTo(String memberTo) {
        this.memberTo = memberTo;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberTo='" + memberTo + '\'' +
                ", agentId='" + agentId + '\'' +
                '}';
    }
}
