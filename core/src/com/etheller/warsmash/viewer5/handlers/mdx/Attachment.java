package com.etheller.warsmash.viewer5.handlers.mdx;

import com.etheller.warsmash.parsers.mdlx.AnimationMap;

public class Attachment extends GenericObject {
	protected final String path;
	protected final int attachmentId;
	protected MdxModel internalModel;

	public Attachment(final MdxModel model, final com.etheller.warsmash.parsers.mdlx.Attachment attachment,
			final int index) {
		super(model, attachment, index);

		final String path = attachment.getPath().replace("\\", "/").toLowerCase().replace(".mdl", ".mdx");

		this.path = path;
		this.attachmentId = attachment.getAttachmentId();
		this.internalModel = null;

		// Second condition is against custom resources using arbitrary paths
		if (!path.equals("") && (path.indexOf(".mdx") != -1)) {
			this.internalModel = (MdxModel) model.viewer.load(path, model.pathSolver, model.solverParams);
		}
	}

	@Override
	public int getVisibility(final float[] out, final int sequence, final int frame, final int counter) {
		return this.getScalarValue(out, AnimationMap.KATV.getWar3id(), sequence, frame, counter, 1);
	}
}