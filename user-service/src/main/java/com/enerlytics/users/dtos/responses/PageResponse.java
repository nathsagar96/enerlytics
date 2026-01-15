package com.enerlytics.users.dtos.responses;

import java.util.List;

public record PageResponse<T>(List<T> content, int pageNumber, int pageSize, int totalPages, int numberOfElements) {}
