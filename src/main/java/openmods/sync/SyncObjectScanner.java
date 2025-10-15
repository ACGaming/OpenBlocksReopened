package openmods.sync;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.util.List;
import openmods.utils.FieldsSelector;

public class SyncObjectScanner extends FieldsSelector {
	public static final SyncObjectScanner INSTANCE = new SyncObjectScanner();

	@Override
	protected List<FieldEntry> listFields(Class<?> cls) {
		List<FieldEntry> result = Lists.newArrayList();

		while (cls != Object.class) {
			for (Field f : cls.getDeclaredFields())
				if (ISyncableObject.class.isAssignableFrom(f.getType())) result.add(new FieldEntry(f, 0));

			cls = cls.getSuperclass();
		}

		return result;
	}

	public void registerAllFields(SyncMap map, Object target) {
		for (Field field : getFields(target.getClass())) {
			ISyncableObject obj;
			try {
				obj = (ISyncableObject)field.get(target);
				Preconditions.checkNotNull(obj, "Field not initialized");
			} catch (Exception e) {
				throw new RuntimeException(String.format("Exception while registering synced field '%s' of object '%s'", field, target), e);
			}

			final String fieldName = field.getName();
			map.registerObject(fieldName, obj);
		}
	}

}
