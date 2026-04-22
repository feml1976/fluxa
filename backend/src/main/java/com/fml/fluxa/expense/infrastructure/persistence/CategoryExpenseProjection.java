package com.fml.fluxa.expense.infrastructure.persistence;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {
    Long getCategoryId();
    BigDecimal getTotal();
}
