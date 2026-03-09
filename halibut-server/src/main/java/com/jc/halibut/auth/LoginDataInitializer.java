package com.jc.halibut.auth;

import com.google.inject.Inject;
import com.jc.halibut.Entity.LoginRole;

import java.util.ArrayList;
import java.util.List;

@com.google.inject.Singleton
public class LoginDataInitializer {
    @Inject
    public LoginDataInitializer(LoginAccountRepository accountRepository) {
        List<LoginAccountRepository.SeedAccount> devAccounts = new ArrayList<>();

        devAccounts.add(new LoginAccountRepository.SeedAccount(
                "admin",
                "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                "Administrator",
                LoginRole.ADMIN,
                true
        ));

        devAccounts.add(new LoginAccountRepository.SeedAccount(
                "manager",
                "6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17",
                "Manager User",
                LoginRole.MANAGER,
                false
        ));

        for (int i = 1; i <= 20; i++) {
            devAccounts.add(new LoginAccountRepository.SeedAccount(
                    "user" + i,
                    "04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb",
                    "User " + i,
                    LoginRole.USER,
                    false
            ));
        }

        accountRepository.saveSeedAccountsIfTableEmpty(devAccounts);
    }
}
