package com.ulfric.testing.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

enum ReflectionHelper {

	;

    private static final String MODIFIERS_FIELD = "modifiers";

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    static void setStaticFinalField(Field field, Object value) throws NoSuchFieldException, IllegalAccessException
    {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField(MODIFIERS_FIELD);

        modifiersField.setAccessible(true);

        int modifiers = modifiersField.getInt(field);

        modifiers &= ~Modifier.FINAL;

        modifiersField.setInt(field, modifiers);

        FieldAccessor fa = ReflectionHelper.REFLECTION_FACTORY.newFieldAccessor(field, false);

        fa.set(null, value);
    }

    static <E> ConstructorAccessor findConstructorAccessor(Class[] additionalParameterTypes, Class<E> clazz) throws NoSuchMethodException
    {
        Class[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(
                additionalParameterTypes, 0,
                parameterTypes, 2,
                additionalParameterTypes.length
        );
        Constructor<E> cstr = clazz.getDeclaredConstructor(parameterTypes);

        return ReflectionHelper.REFLECTION_FACTORY.newConstructorAccessor(cstr);
    }

	static <E> E constructEnum(Class<E> clazz, ConstructorAccessor ca, String value, int ordinal, Object[] additional) throws Exception
	{
		Object[] parms = new Object[additional.length + 2];
		parms[0] = value;
		parms[1] = ordinal;

		System.arraycopy(additional, 0, parms, 2, additional.length);

		return clazz.cast(ca.newInstance(parms));
	}

}