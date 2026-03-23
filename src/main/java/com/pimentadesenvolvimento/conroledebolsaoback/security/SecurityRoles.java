package com.pimentadesenvolvimento.conroledebolsaoback.security;

public final class SecurityRoles {
    public static final String NAME_ROLE_ADMIN = "ROLE_ADMIN";
    public static final String NAME_ROLE_USER = "ROLE_USER";
    public static final String NAME_ROLE_SUPERVISOR = "ROLE_SUPERVISOR";
    private static final String ADMIN_LITERAL = "'" + NAME_ROLE_ADMIN + "'";
    public static final String HAS_ADMIN = "hasAuthority(" + ADMIN_LITERAL + ")";
    private static final String USER_LITERAL = "'" + NAME_ROLE_USER + "'";
    public static final String HAS_USER = "hasAuthority(" + USER_LITERAL + ")";
    public static final String HAS_ANY_USER_OR_ADMIN = "hasAnyAuthority(" + ADMIN_LITERAL + ", " + USER_LITERAL + ")";

    private SecurityRoles() {
    }
}