package com.etheller.warsmash.viewer5.handlers.w3x.simulation.orders;

import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CSimulation;

public interface CBehavior {
	/**
	 * Executes one step of game simulation of the current order, returning true if
	 * the order has completed. Many orders may wrap the move order and spend a
	 * number of simulation steps moving to get within range of the target point
	 * before completing.
	 *
	 * @return
	 */
	boolean update(CSimulation game);

	/**
	 * Gets the Order ID of the order, useful for determining which icon to
	 * highlight on the unit's command card.
	 *
	 * @return
	 */
	int getOrderId();
}