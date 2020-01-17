package com.etheller.warsmash.viewer5.handlers.mdx;

import java.util.ArrayList;
import java.util.Arrays;

import com.etheller.warsmash.parsers.mdlx.timeline.Timeline;
import com.etheller.warsmash.util.RenderMathUtils;

public final class SdSequence<TYPE> {

	private final Sd<TYPE> sd;
	public final long start; // UInt32
	public final long end; // UInt32
	public long[] frames;
	public TYPE[] values;
	public TYPE[] inTans;
	public TYPE[] outTans;
	public boolean constant;

	public SdSequence(final Sd<TYPE> sd, final long start, final long end, final Timeline<TYPE> timeline,
			final boolean isGlobalSequence, final SdArrayDescriptor<TYPE> arrayDescriptor) {
		this.sd = sd;
		this.start = start;
		this.end = end;
		final ArrayList<Long> framesBuilder = new ArrayList<>();
		final ArrayList<TYPE> valuesBuilder = new ArrayList<>();
		final ArrayList<TYPE> inTansBuilder = new ArrayList<>();
		final ArrayList<TYPE> outTansBuilder = new ArrayList<>();
		this.constant = false;

		final int interpolationType = sd.interpolationType;
		final long[] frames = timeline.getFrames();
		final TYPE[] values = timeline.getValues();
		final TYPE[] inTans = timeline.getInTans();
		final TYPE[] outTans = timeline.getOutTans();
		final TYPE defval = sd.defval;

		// When using a global sequence, where the first key is outside of the
		// sequence's length, it becomes its constant value.
		// When having one key in the sequence's range, and one key outside of
		// it, results seem to be non-deterministic.
		// Sometimes the second key is used too, sometimes not.
		// It also differs depending where the model is viewed - the WE
		// previewer, the WE itself, or the game.
		// All three show different results, none of them make sense.
		// Therefore, only handle the case where the first key is outside.
		// This fixes problems spread over many models, e.g. HeroMountainKing
		// (compare in WE and in Magos).
		if (isGlobalSequence && (frames.length > 0) && (frames[0] > end)) {
			this.frames[0] = frames[0];
			this.values[0] = values[0];
		}

		// Go over the keyframes, and add all of the ones that are in this
		// sequence (start <= frame <= end).
		for (int i = 0, l = frames.length; i < l; i++) {
			final long frame = frames[i];

			if ((frame >= start) && (frame <= end)) {
				framesBuilder.add(frame);
				valuesBuilder.add(values[i]);

				if (interpolationType > 1) {
					inTansBuilder.add(inTans[i]);
					outTansBuilder.add(outTans[i]);
				}
			}
		}

		final int keyframeCount = framesBuilder.size();

		if (keyframeCount == 0) {
			// if there are no keys, use the default value directly.
			this.constant = true;
			framesBuilder.add(start);
			valuesBuilder.add(defval);
		}
		else if (keyframeCount == 1) {
			// If there's only one key, use it directly
			this.constant = true;
		}
		else {
			final TYPE firstValue = valuesBuilder.get(0);

			// If all of the values in this sequence are the same, might as well
			// make it constant.
			boolean allFramesMatch = true;
			for (final TYPE value : valuesBuilder) {
				if (!equals(firstValue, value)) {
					allFramesMatch = false;
				}
			}
			this.constant = allFramesMatch;

			if (!this.constant) {
				// If there is no opening keyframe for this sequence, inject one
				// with the default value.
				if (framesBuilder.get(0) != start) {
					framesBuilder.add(start);
					valuesBuilder.add(defval);

					if (interpolationType > 1) {
						inTansBuilder.add(defval);
						outTansBuilder.add(defval);
					}
				}

				// If there is no closing keyframe for this sequence, inject one
				// with the default value.
				if (framesBuilder.get(framesBuilder.size() - 1) != end) {
					framesBuilder.add(end);
					valuesBuilder.add(valuesBuilder.get(0));

					if (interpolationType > 1) {
						inTansBuilder.add(inTansBuilder.get(0));
						outTansBuilder.add(outTansBuilder.get(0));
					}
				}
			}
		}
		this.frames = new long[framesBuilder.size()];
		for (int i = 0; i < framesBuilder.size(); i++) {
			this.frames[i] = framesBuilder.get(i);
		}
		this.values = valuesBuilder.toArray(arrayDescriptor.create(valuesBuilder.size()));
		this.inTans = inTansBuilder.toArray(arrayDescriptor.create(inTansBuilder.size()));
		this.outTans = outTansBuilder.toArray(arrayDescriptor.create(outTansBuilder.size()));
	}

//	private TYPE[] makeArray(final int size) {
//		return (TYPE[]) new Object[size];
//	}

	public int getValue(final TYPE out, final long frame) {
		final int l = this.frames.length;

		if (this.constant || (frame < this.start)) {
			this.sd.copy(out, this.values[0]);

			return -1;
		}
		else if (frame >= this.end) {
			this.sd.copy(out, this.values[l - 1]);

			return l - 1;
		}
		else {
			for (int i = 1; i < l; i++) {
				if (this.frames[i] > frame) {
					final long start = this.frames[i = 1];
					final long end = this.frames[i];
					final float t = RenderMathUtils.clamp(((end - start) == 0 ? 0 : ((frame - start) / (end - start))),
							0, 1);

					this.sd.interpolate(out, this.values, this.inTans, this.outTans, i - 1, i, t);

					return i;
				}
			}

			return -1;
		}
	}

	protected final boolean equals(final TYPE a, final TYPE b) {
		if ((a instanceof Float) && (b instanceof Float)) {
			return a.equals(b);
		}
		else if ((a instanceof Long) && (b instanceof Long)) {
			return a.equals(b);
		}
		else if ((a instanceof float[]) && (b instanceof float[])) {
			return Arrays.equals(((float[]) a), (float[]) b);
		}
		return false;
	}
}