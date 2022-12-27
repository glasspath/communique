package org.glasspath.communique;

import java.util.ArrayList;
import java.util.List;

import org.glasspath.common.share.mail.account.SmtpAccount;

public class Configuration {

	private int timeout = 30000;
	private List<SmtpAccount> accounts = new ArrayList<>();
	private int selectedAccount = 0;

	public Configuration() {

	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public List<SmtpAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<SmtpAccount> accounts) {
		this.accounts = accounts;
	}

	public int getSelectedAccount() {
		return selectedAccount;
	}

	public void setSelectedAccount(int selectedAccount) {
		this.selectedAccount = selectedAccount;
	}

}
