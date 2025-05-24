package com.zzh.droidlock;

import android.accounts.Account;

interface IUserService {
    String execute(String command) = 1;
    int getUid() = 2;
    Account[] listAccounts() = 3;
    void destroy() = 16777114;
}
