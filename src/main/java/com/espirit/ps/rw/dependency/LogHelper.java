package com.espirit.ps.rw.dependency;

import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.editor.reference.Reference;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.or.schema.Entity;

public class LogHelper {
	
	/**
	 * @param object Firstspirit Object, either IDProvider or StoreElement
	 * @return String representation for easy identification within FS Project
	 */
	public static String getTextIdentification(Object object) {
		
		if (object == null) {
			return "Tried to retrieve textid from NULL";
		}
		
		if (object instanceof Dataset) {
			Dataset dataset = (Dataset) object;
			return dataset.getEntity().getEntityType().getName() + "(" + dataset.getId() + ")";
		}
		if (object instanceof IDProvider && ((IDProvider) object).hasUid()) {
			IDProvider idProvider = (IDProvider) object;
			return idProvider.getUid() + "(" + idProvider.getId() + ")";
		}
		if (object instanceof StoreElement) {
			StoreElement storeElement = (StoreElement) object;
			return storeElement.getReferenceName() + "(" + storeElement.getStore().getName() + ")";
		}
		if (object instanceof Entity) {
			Entity entity = (Entity) object;
			return entity.getIdentifier().getEntityTypeName() + "(" + entity.get("fs_id") + ")";
		}
		if (object instanceof ReferenceEntry) {
			ReferenceEntry referenceEntry = (ReferenceEntry) object;
			return referenceEntry.getReferenceString() + "(" + referenceEntry.getId() + ")";
		}
		if (object instanceof Reference) {
			Reference reference = (Reference) object;
			return reference.getReferenceString() + "(" + reference.getTypeName() + ")";
		}
		
		return "Unknown object type: " + object.toString();
	}
}
