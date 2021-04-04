package com.etheller.warsmash.viewer5.handlers.w3x.simulation.region;

import com.badlogic.gdx.math.Rectangle;

public class CRegion {
	private Rectangle currentBounds;
	private boolean complexRegion;

	public void addRect(final Rectangle rect, final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			this.currentBounds = new Rectangle(rect);
			regionManager.addRectForRegion(this, this.currentBounds);
		}
		else {
			if (!this.complexRegion) {
				convertToComplexRegionAndAddRect(rect, regionManager);
			}
			else {
				complexRegionAddRect(rect, regionManager);
			}
		}
	}

	public void clearRect(final Rectangle rect, final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			return;
		}
		if (this.complexRegion) {
			regionManager.removeRectForRegion(this, this.currentBounds);
			regionManager.removeComplexRegionCells(this, rect);
			regionManager.computeNewMinimumComplexRegionBounds(this, this.currentBounds);
			regionManager.addRectForRegion(this, this.currentBounds);
		}
		else {
			this.complexRegion = true;
			regionManager.addComplexRegionCells(this, this.currentBounds);
			regionManager.removeComplexRegionCells(this, rect);
		}
	}

	public void remove(final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			return;
		}
		if (this.complexRegion) {
			regionManager.removeComplexRegionCells(this, this.currentBounds);
		}
		regionManager.removeRectForRegion(this, this.currentBounds);
	}

	public void addCell(final float x, final float y, final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			this.complexRegion = true;
			this.currentBounds = new Rectangle(x, y, 0, 0);
			regionManager.addComplexRegionCell(this, x, y, this.currentBounds);
			regionManager.addRectForRegion(this, this.currentBounds);
		}
		else {
			regionManager.removeRectForRegion(this, this.currentBounds);
			if (!this.complexRegion) {
				regionManager.addComplexRegionCells(this, this.currentBounds);
				this.complexRegion = true;
			}
			regionManager.addComplexRegionCell(this, x, y, this.currentBounds);
			regionManager.addRectForRegion(this, this.currentBounds);
		}
	}

	public void clearCell(final float x, final float y, final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			return;
		}
		else {
			regionManager.removeRectForRegion(this, this.currentBounds);
			if (!this.complexRegion) {
				regionManager.addComplexRegionCells(this, this.currentBounds);
				this.complexRegion = true;
			}
			regionManager.clearComplexRegionCell(this, x, y, this.currentBounds);
			regionManager.addRectForRegion(this, this.currentBounds);
		}
	}

	private void complexRegionAddRect(final Rectangle rect, final CRegionManager regionManager) {
		regionManager.removeRectForRegion(this, this.currentBounds);
		regionManager.addComplexRegionCells(this, rect);
		this.currentBounds = this.currentBounds.merge(rect);
		regionManager.addRectForRegion(this, this.currentBounds);
	}

	private void convertToComplexRegionAndAddRect(final Rectangle rect, final CRegionManager regionManager) {
		regionManager.removeRectForRegion(this, this.currentBounds);
		this.complexRegion = true;
		regionManager.addComplexRegionCells(this, this.currentBounds);
		regionManager.addComplexRegionCells(this, rect);
		this.currentBounds = this.currentBounds.merge(rect);
		regionManager.addRectForRegion(this, this.currentBounds);
	}

	public Rectangle getCurrentBounds() {
		return this.currentBounds;
	}

	public void setCurrentBounds(final Rectangle currentBounds) {
		this.currentBounds = currentBounds;
	}

	public boolean isComplexRegion() {
		return this.complexRegion;
	}

	public void setComplexRegion(final boolean complexRegion) {
		this.complexRegion = complexRegion;
	}

	public boolean contains(final float x, final float y, final CRegionManager regionManager) {
		if (this.currentBounds == null) {
			return false;
		}
		if (this.complexRegion) {
			return regionManager.isPointInComplexRegion(this, x, y);
		}
		return this.currentBounds.contains(x, y);
	}
}
