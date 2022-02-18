/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.fundingsummary;

import com.querydsl.core.annotations.QueryEntity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@QueryEntity
public class FundingSummaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "funding_summary_record_seq_gen")
    @SequenceGenerator(name = "funding_summary_record_seq_gen", sequenceName = "funding_summary_record_seq",
        initialValue = 100, allocationSize = 1)
    private Integer id;

    private Integer academicYear;

    private Integer period;

    private Integer actualYear;

    private Integer actualMonth;

    private Integer ukprn;

    private String fundingLine;

    private String esfaFundingLine;

    private String source;

    private String category;

    private String esfaFundingCategory;

    private BigDecimal monthTotal;

    private BigDecimal totalPayment;

    private String providerName;

    public Integer getId() {
        return id;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public Integer getPeriod() {
        return period;
    }

    public Integer getActualYear() {
        return actualYear;
    }

    public Integer getActualMonth() {
        return actualMonth;
    }

    public Integer getUkprn() {
        return ukprn;
    }

    public String getFundingLine() {
        return fundingLine;
    }

    public String getSource() {
        return source;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getMonthTotal() {
        return monthTotal;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public String getEsfaFundingLine() {
        return esfaFundingLine;
    }

    public String getEsfaFundingCategory() {
        return esfaFundingCategory;
    }

    public String getProviderName() {
        return providerName;
    }

    public FundingSummaryRecord() {}

    public FundingSummaryRecord(Integer academicYear, Integer period, Integer actualYear, Integer actualMonth, Integer ukprn,
                         String fundingLine, String source, String category, BigDecimal monthTotal, BigDecimal totalPayment,
                                String esfaFundingLine, String esfaFundingCategory) {
        this.academicYear = academicYear;
        this.period = period;
        this.actualYear = actualYear;
        this.actualMonth = actualMonth;
        this.ukprn = ukprn;
        this.fundingLine = fundingLine;
        this.source = source;
        this.category = category;
        this.monthTotal = monthTotal;
        this.totalPayment = totalPayment;
        this.esfaFundingLine = esfaFundingLine;
        this.esfaFundingCategory = esfaFundingCategory;
    }

    public FundingSummaryRecord(Integer academicYear, Integer period, Integer actualYear, Integer actualMonth,
                                Integer ukprn, String fundingLine, String source, String category,
                                BigDecimal monthTotal, BigDecimal totalPayment, String esfaFundingLine,
                                String esfaFundingCategory, String providerName) {
        this(academicYear, period, actualYear, actualMonth, ukprn, fundingLine,
                source, category, monthTotal, totalPayment, esfaFundingLine, esfaFundingCategory);
        this.providerName = providerName;
    }

}
