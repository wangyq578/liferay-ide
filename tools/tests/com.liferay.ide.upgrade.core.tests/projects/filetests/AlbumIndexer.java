/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package org.liferay.jukebox.util;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.RelatedEntryIndexer;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.document.library.kernel.model.DLFileEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;

import org.liferay.jukebox.model.Album;
import org.liferay.jukebox.model.Artist;
import org.liferay.jukebox.service.AlbumLocalServiceUtil;
import org.liferay.jukebox.service.ArtistLocalServiceUtil;
import org.liferay.jukebox.service.permission.AlbumPermission;

/**
 * @author Eudaldo Alonso
 */
public class AlbumIndexer extends BaseIndexer implements RelatedEntryIndexer {

	public static final String[] CLASS_NAMES = {Album.class.getName()};

	public static final String PORTLET_ID = PortletKeys.ALBUMS;

	public AlbumIndexer() {
		setPermissionAware(true);
	}

	@Override
	public void addRelatedEntryFields(Document document, Object obj)
		throws Exception {

		if (obj instanceof DLFileEntry) {
			DLFileEntry dlFileEntry = (DLFileEntry)obj;

			Album album = AlbumLocalServiceUtil.getAlbum(
				GetterUtil.getLong(dlFileEntry.getTitle()));

			document.addKeyword(
				Field.CLASS_NAME_ID,
				PortalUtil.getClassNameId(Album.class.getName()));
			document.addKeyword(Field.CLASS_PK, album.getAlbumId());
			document.addKeyword(Field.RELATED_ENTRY, true);
		}
	}

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	public boolean hasPermission(
			PermissionChecker permissionChecker, String entryClassName,
			long entryClassPK, String actionId)
		throws Exception {

		return AlbumPermission.contains(
			permissionChecker, entryClassPK, ActionKeys.VIEW);
	}

	@Override
	public void postProcessContextBooleanFilter(BooleanFilter contextBooleanFilter, SearchContext searchContext) throws Exception {
		addStatus(contextBooleanFilter, searchContext);
	}

	@Override
	public void postProcessSearchQuery(
			BooleanQuery searchQuery, SearchContext searchContext)
		throws Exception {

		if (searchContext.getAttributes() == null) {
			return;
		}

		addSearchTerm(searchQuery, searchContext, Field.TITLE, true);
		addSearchTerm(searchQuery, searchContext, "artist", true);
		addSearchTerm(searchQuery, searchContext, "year", false);
	}

	@Override
	protected void doDelete(Object obj) throws Exception {
		Album album = (Album)obj;

		deleteDocument(album.getCompanyId(), album.getAlbumId());
	}

	@Override
	protected Document doGetDocument(Object obj) throws Exception {
		Album album = (Album)obj;

		Document document = getBaseModelDocument(PORTLET_ID, album);

		document.addDate(Field.MODIFIED_DATE, album.getModifiedDate());
		document.addText(Field.TITLE, album.getName());
		document.addKeyword("year", album.getYear());

		Artist artist = ArtistLocalServiceUtil.getArtist(album.getArtistId());

		document.addText("artist", artist.getName());
		document.addKeyword("artistId", artist.getArtistId());

		return document;
	}

	@Override
	protected Summary doGetSummary(
		Document document, Locale locale, String snippet,
		PortletRequest portletRequest, PortletResponse portletResponse) throws Exception {

		Summary summary = createSummary(document);

		summary.setMaxContentLength(200);

		return summary;
	}

	@Override
	protected void doReindex(Object obj) throws Exception {
		Album album = (Album)obj;

		Document document = getDocument(album);
	}

	@Override
	protected void doReindex(String className, long classPK) throws Exception {
		Album album = AlbumLocalServiceUtil.getAlbum(classPK);

		doReindex(album);
	}

	@Override
	protected void doReindex(String[] ids) throws Exception {
		long companyId = GetterUtil.getLong(ids[0]);

		reindexEntries(companyId);
	}

	@Override
	protected String getPortletId(SearchContext searchContext) {
		return PORTLET_ID;
	}

	protected void reindexEntries(long companyId) throws PortalException {
		final Collection<Document> documents = new ArrayList<Document>();

		ActionableDynamicQuery actionableDynamicQuery = AlbumLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setCompanyId(companyId);

		actionableDynamicQuery.performActions();

		SearchEngineUtil.updateDocuments(
			getSearchEngineId(), companyId, documents);
	}

	@Override
	public String getClassName() {
		return Album.class.getName();
	}

	@Override
	public void addRelatedClassNames(BooleanFilter arg0, SearchContext arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFullQuery(SearchContext arg0) {
		// TODO Auto-generated method stub
		
	}

}