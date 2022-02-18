/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation;

import com.querydsl.core.annotations.QueryEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "provider_allocation")
@QueryEntity
public class ProviderAllocation {

    @Id
    public ProviderAllocationPK id;
    public BigDecimal fullTermAllocation;
    public BigDecimal yearlyAllocation;
    @Column(name = "ytd_allocation_r01")
    public BigDecimal ytdAllocationR01;
    @Column(name = "ytd_allocation_r02")
    public BigDecimal ytdAllocationR02;
    @Column(name = "ytd_allocation_r03")
    public BigDecimal ytdAllocationR03;
    @Column(name = "ytd_allocation_r04")
    public BigDecimal ytdAllocationR04;
    @Column(name = "ytd_allocation_r05")
    public BigDecimal ytdAllocationR05;
    @Column(name = "ytd_allocation_r06")
    public BigDecimal ytdAllocationR06;
    @Column(name = "ytd_allocation_r07")
    public BigDecimal ytdAllocationR07;
    @Column(name = "ytd_allocation_r08")
    public BigDecimal ytdAllocationR08;
    @Column(name = "ytd_allocation_r09")
    public BigDecimal ytdAllocationR09;
    @Column(name = "ytd_allocation_r10")
    public BigDecimal ytdAllocationR10;
    @Column(name = "ytd_allocation_r11")
    public BigDecimal ytdAllocationR11;
    @Column(name = "ytd_allocation_r12")
    public BigDecimal ytdAllocationR12;
    @Column(name = "ytd_allocation_r13")
    public BigDecimal ytdAllocationR13;
    @Column(name = "ytd_allocation_r14")
    public BigDecimal ytdAllocationR14;

    public List<BigDecimal> getYtdAllocations() {
        List<BigDecimal> ytdAllocations = new ArrayList<BigDecimal>();
        ytdAllocations.add(ytdAllocationR01);
        ytdAllocations.add(ytdAllocationR02);
        ytdAllocations.add(ytdAllocationR03);
        ytdAllocations.add(ytdAllocationR04);
        ytdAllocations.add(ytdAllocationR05);
        ytdAllocations.add(ytdAllocationR06);
        ytdAllocations.add(ytdAllocationR07);
        ytdAllocations.add(ytdAllocationR08);
        ytdAllocations.add(ytdAllocationR09);
        ytdAllocations.add(ytdAllocationR10);
        ytdAllocations.add(ytdAllocationR11);
        ytdAllocations.add(ytdAllocationR12);
        ytdAllocations.add(ytdAllocationR13);
        ytdAllocations.add(ytdAllocationR14);
        return ytdAllocations;
    }

    public ProviderAllocation(ProviderAllocationPK id, BigDecimal fullTermAllocation, BigDecimal yearlyAllocation,
            BigDecimal ytdAllocationR01, BigDecimal ytdAllocationR02, BigDecimal ytdAllocationR03, BigDecimal ytdAllocationR04,
            BigDecimal ytdAllocationR05, BigDecimal ytdAllocationR06, BigDecimal ytdAllocationR07, BigDecimal ytdAllocationR08,
            BigDecimal ytdAllocationR09, BigDecimal ytdAllocationR10, BigDecimal ytdAllocationR11, BigDecimal ytdAllocationR12,
            BigDecimal ytdAllocationR13, BigDecimal ytdAllocationR14) {
        this.id = id;
        this.fullTermAllocation = fullTermAllocation;
        this.yearlyAllocation = yearlyAllocation;
        this.ytdAllocationR01 = ytdAllocationR01;
        this.ytdAllocationR02 = ytdAllocationR02;
        this.ytdAllocationR03 = ytdAllocationR03;
        this.ytdAllocationR04 = ytdAllocationR04;
        this.ytdAllocationR05 = ytdAllocationR05;
        this.ytdAllocationR06 = ytdAllocationR06;
        this.ytdAllocationR07 = ytdAllocationR07;
        this.ytdAllocationR08 = ytdAllocationR08;
        this.ytdAllocationR09 = ytdAllocationR09;
        this.ytdAllocationR10 = ytdAllocationR10;
        this.ytdAllocationR11 = ytdAllocationR11;
        this.ytdAllocationR12 = ytdAllocationR12;
        this.ytdAllocationR13 = ytdAllocationR13;
        this.ytdAllocationR14 = ytdAllocationR14;
    }

    public ProviderAllocation() {
    }
}






