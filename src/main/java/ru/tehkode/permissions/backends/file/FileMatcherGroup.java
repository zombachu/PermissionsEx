package ru.tehkode.permissions.backends.file;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import ru.tehkode.permissions.backends.memory.MemoryMatcherGroup;
import ru.tehkode.permissions.data.MatcherGroup;
import ru.tehkode.permissions.data.Qualifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Memory matcher group supporting additional matcher data from files
 */
public final class FileMatcherGroup extends MemoryMatcherGroup<FileMatcherGroup, FileMatcherList> {
	private static final Logger LOGGER = Logger.getLogger(FileMatcherGroup.class.getCanonicalName());
	private final List<String> comments;
	private final Multimap<String, String> entryComments;

	FileMatcherGroup(String name, AtomicReference<FileMatcherGroup> selfRef, FileMatcherList listRef, Multimap<Qualifier, String> qualifiers, Map<String, String> entries, List<String> comments, Multimap<String, String> entryComments) {
		super(name, selfRef, listRef, qualifiers, entries);
		this.comments = comments == null ? null : Collections.unmodifiableList(comments);
		this.entryComments = entryComments == null ? null : ImmutableMultimap.copyOf(entryComments);
	}

	FileMatcherGroup(String name, AtomicReference<FileMatcherGroup> selfRef, FileMatcherList listRef, Multimap<Qualifier, String> qualifiers, List<String> entriesList, List<String> comments, Multimap<String, String> entryComments) {
		super(name, selfRef, listRef, qualifiers, entriesList);
		this.comments = comments == null ? null : Collections.unmodifiableList(comments);
		this.entryComments = entryComments == null ? null : ImmutableMultimap.copyOf(entryComments);
	}

	public List<String> getComments() {
		return comments;
	}

	public Multimap<String, String> getEntryComments() {
		return entryComments;
	}

	@Override
	protected ListenableFuture<MatcherGroup> setQualifiersImpl(Multimap<Qualifier, String> qualifiers) {
		return Futures.transform(super.setQualifiersImpl(qualifiers), new Function<MatcherGroup, MatcherGroup>() {
			@Override
			public MatcherGroup apply(@Nullable MatcherGroup matcherGroup) {
				save();
				return matcherGroup;
			}
		}, listRef.getExecutor());
	}

	@Override
	protected ListenableFuture<MatcherGroup> setEntriesImpl(Map<String, String> entries) {
		return Futures.transform(super.setEntriesImpl(entries), new Function<MatcherGroup, MatcherGroup>() {
			@Override
			public MatcherGroup apply(@Nullable MatcherGroup matcherGroup) {
				save();
				return matcherGroup;
			}
		}, listRef.getExecutor());
	}

	@Override
	protected ListenableFuture<MatcherGroup> setEntriesImpl(List<String> entries) {
		return Futures.transform(super.setEntriesImpl(entries), new Function<MatcherGroup, MatcherGroup>() {
			@Override
			public MatcherGroup apply(@Nullable MatcherGroup matcherGroup) {
				save();
				return matcherGroup;
			}
		}, listRef.getExecutor());
	}

	@Override
	protected ListenableFuture<Boolean> removeImpl() {
		return Futures.transform(super.removeImpl(), new Function<Boolean, Boolean>() {
			@Override
			public Boolean apply(@Nullable Boolean val) {
				if (val != null && val) {
					save();
				}
				return val;
			}
		}, listRef.getExecutor());
	}

	private void save() {
		try {
			listRef.save();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while saving for group " + this, e);
		}
	}

	@Override
	protected FileMatcherGroup newSelf(Map<String, String> entries, Multimap<Qualifier, String> qualifiers) {
		return new FileMatcherGroup(getName(), selfRef, listRef, qualifiers, entries, getComments(), getEntryComments());
	}

	@Override
	protected FileMatcherGroup newSelf(List<String> entries, Multimap<Qualifier, String> qualifiers) {
		return new FileMatcherGroup(getName(), selfRef, listRef, qualifiers, entries, getComments(), getEntryComments());
	}

	@Override
	public String toString() {
		final FileMatcherGroup selfRefValue = selfRef.get();
		return "FileMatcherGroup{name=" + getName()
				+ ",selfRef=" + (selfRefValue == null ? "null" : selfRefValue == this ? "me" : selfRefValue)
				+ ",listRef=" + listRef
				+ ",entries=" + getEntries()
				+ ",qualifiers=" + getQualifiers()
				+ ",comments=" + getComments()
				+ ",entryComments=" + getEntryComments()
				+ "}";
	}
}
