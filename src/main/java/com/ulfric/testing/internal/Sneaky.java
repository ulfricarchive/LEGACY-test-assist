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

	static <T> T tryTo(SneakySupplier<T> supplier)
	{
		try
		{
			return supplier.supply();
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
	interface SneakySupplier<T> {
		T supply() throws Throwable;
	}

}
