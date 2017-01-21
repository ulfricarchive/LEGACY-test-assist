package com.ulfric.testing.internal;

enum Sneaky {

	;

	static void tryTo(SneakyRunnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch (Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}

	static <T> T tryTo(SneakyProvider<T> provider)
	{
		try
		{
			return provider.provide();
		}
		catch (Throwable throwable)
		{
			throw new RuntimeException(throwable);
		}
	}

	@FunctionalInterface
	interface SneakyRunnable {
		void run() throws Throwable;
	}

	@FunctionalInterface
	interface SneakyProvider<T> {
		T provide() throws Throwable;
	}

}
