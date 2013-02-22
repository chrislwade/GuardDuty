package net.mortu.guardduty.guard;

import org.bukkit.entity.Player;

public interface Guard {
	
	public Player getPlayer();
	public boolean isOnDuty();
	public Double getOnDutySeconds();
	public Double getOffDutySeconds();
	public void setOnDuty();
	public void setOffDuty();
	public String getDisplayName();
	public Double getSalary();
	public boolean deposit(Double amount);
	public boolean withdraw(Double amount);
	
}
