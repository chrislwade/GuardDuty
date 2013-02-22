package net.mortu.guardduty.guard;

import org.bukkit.entity.Player;

public class NullGuard implements Guard {

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOnDuty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Double getOnDutySeconds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getOffDutySeconds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOnDuty() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOffDuty() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getSalary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deposit(Double amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean withdraw(Double amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
