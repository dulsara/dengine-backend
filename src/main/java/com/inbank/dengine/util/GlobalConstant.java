package com.inbank.dengine.util;

import java.math.BigDecimal;

import static com.inbank.dengine.util.GlobalConstant.LoanConstraints.*;

public class GlobalConstant {

    public static class LoanConstraints {
        public static final BigDecimal MINIMUM_SUM = BigDecimal.valueOf(2000);
        public static final BigDecimal MAXIMUM_SUM = BigDecimal.valueOf(10000);
        public static final Integer MAXIMUM_PERIOD = 60;
        public static final Integer MINIMUM_PERIOD = 12;
    }

    public static class LoanDecisionMessages {

        public static final String LOAN_APPROVED = "Your Loan request has been APPROVED.";
        public static final String LOAN_REJECTED = "Requested loan request has been REJECTED.";
        public static final String SUGGESTED_AMOUNT = "Bank Suggested Amount is here ";
        public static final String SUGGESTED_PERIOD = "Bank Suggested Period : ";
    }

    public static class LoanDecisionErrors {
        public static final String LESS_THAN_MINIMUM_AMOUNT = "Requested Loan amount is less than : " + MINIMUM_SUM;
        public static final String HIGHER_THAN_MAXIMUM_AMOUNT = "Requested Loan amount is higher than : " + MAXIMUM_SUM;
        public static final String LESS_THAN_MINIMUM_PERIOD = "Requested Loan Period is less than : " + MINIMUM_PERIOD;
        public static final String MORE_THAN_MAXIMUM_PERIOD = "Requested Loan Period is higher than : " + MAXIMUM_PERIOD;
        public static final String INVALID_USER_ACCOUNT = "No User Account for givenId : ";
        public static final String USER_IS_HAVING_DEBT = "user is having Debt.";
        public static final String INTERNAL_DATA_ERROR = "User data has invalid for userId : ";
    }
}
