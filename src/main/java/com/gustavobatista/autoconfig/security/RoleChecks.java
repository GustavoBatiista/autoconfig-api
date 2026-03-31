package com.gustavobatista.autoconfig.security;

import com.gustavobatista.autoconfig.enums.Role;

/**
 * Helpers para checagem de papéis no domínio (espelha regras de autorização operacional).
 */
public final class RoleChecks {

    private RoleChecks() {
    }

    public static boolean isAdmin(Role role) {
        return role == Role.ROLE_ADMIN;
    }

}
