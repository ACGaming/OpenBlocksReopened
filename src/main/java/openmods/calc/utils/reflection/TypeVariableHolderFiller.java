package openmods.calc.utils.reflection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class TypeVariableHolderFiller {

	private TypeVariableHolderFiller() {}

	public static final TypeVariableHolderFiller instance = new TypeVariableHolderFiller();

	public void initialize(Class<?> targetClass) {
		final Map<Field, Class<?>> targetToSource = Maps.newHashMap();

		fillHolders(targetClass, targetToSource);

		for (Class<?> inner : targetClass.getDeclaredClasses())
			fillHolders(inner, targetToSource);

		fillFields(targetToSource);
	}

	private static void fillHolders(Class<?> targetClass, final Map<Field, Class<?>> targetToSource) {
		final Field[] fields = targetClass.getDeclaredFields();

		final TypeVariableHolder clsAnnotation = targetClass.getAnnotation(TypeVariableHolder.class);
		if (clsAnnotation != null) {
			final Class<?> sourceClass = findSourceClass(clsAnnotation, targetClass);

			for (Field f : fields) {
				if (isValidField(f))
					targetToSource.put(f, sourceClass);
			}
		}

		for (Field f : fields) {
			final TypeVariableHolder fieldAnnotation = f.getAnnotation(TypeVariableHolder.class);
			if (fieldAnnotation != null) {
				Preconditions.checkArgument(isValidField(f), "Field %s marked with TypeVariableHolder annotation must be static, non-final and have TypeVariable type", f);
				final Class<?> sourceClass = findSourceClass(fieldAnnotation, targetClass);
				targetToSource.put(f, sourceClass);
			}
		}
	}

	private static Class<?> findSourceClass(TypeVariableHolder annotation, Class<?> targetClass) {
		if (annotation.value() == TypeVariableHolder.UseDeclaringType.class)
			return targetClass;
		else
			return annotation.value();
	}

	private static boolean isValidField(Field field) {
		final int modifiers = field.getModifiers();
		return field.getType() == TypeVariable.class
				&& Modifier.isStatic(modifiers)
				&& !Modifier.isFinal(modifiers);
	}

	private void fillFields(Map<Field, Class<?>> fieldTargetToSource) {
		for (Map.Entry<Field, Class<?>> e : fieldTargetToSource.entrySet())
			fillField(e.getKey(), e.getValue());
	}

	private final Map<Class<?>, Map<String, TypeVariable<?>>> sourceCache = Maps.newHashMap();

	private void fillField(Field targetField, Class<?> sourceClass) {

		final Map<String, TypeVariable<?>> sourceVariables = getSourceTypeVariables(sourceClass);

		final String variableName = targetField.getName();
		final TypeVariable<?> sourceTypeVariable = sourceVariables.get(variableName);
		Preconditions.checkState(sourceTypeVariable != null, "Can't find type variable '%s' in class '%s", variableName, sourceClass);

		targetField.setAccessible(true);
		try {
			targetField.set(null, sourceTypeVariable);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, TypeVariable<?>> getSourceTypeVariables(Class<?> sourceClass) {
		Map<String, TypeVariable<?>> result = sourceCache.get(sourceClass);
		if (result == null) {
			result = Maps.newHashMap();
			for (TypeVariable<?> t : sourceClass.getTypeParameters())
				result.put(t.getName(), t);
			sourceCache.put(sourceClass, result);
		}
		return result;
	}

}
