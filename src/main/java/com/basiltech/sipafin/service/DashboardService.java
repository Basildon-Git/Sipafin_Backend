package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.DashboardDtos;

import java.time.LocalDate;

public interface DashboardService {

    DashboardDtos.DashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate);
}