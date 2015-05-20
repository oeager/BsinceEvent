package com.developer.bsince.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.Event;
import com.developer.bsince.event.EventFilter;
import com.developer.bsince.response.ResponseResult;

public class ExecutorEngineImp implements IEngine {

	private static final String TAG = "IEngine";

	private final ExecutorService mExecutorService;

	private final AtomicInteger mSerialNumber = new AtomicInteger();

	private final Set<Event<?>> mCurrentTasks = new HashSet<Event<?>>();

	public ExecutorEngineImp(int nThreads) {

		this(new BsinceExecutorService(nThreads, nThreads, 0L,
				TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()));

	}

	public ExecutorEngineImp(ExecutorService mExecutorService) {

		this.mExecutorService = mExecutorService;
	}

	@Override
	public <T> T execute(Event<T> callable) {
		if (HttpGlobalConfiguration.debug) {
			Log.i(TAG, "[Synchronous<Task>]" + callable.toString());
		}
		try {
			synchronized (mCurrentTasks) {
				mCurrentTasks.add(callable);
			}
			callable.setTaskSerialNumber(mSerialNumber.incrementAndGet());
			mExecutorService.submit(callable).get();
			// mExecutorService.ex
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public <T> void submit(Event<T> callable) {
		if (HttpGlobalConfiguration.debug) {
			Log.i(TAG, "[Asynchronous<Task>]" + callable.toString());
		}
		synchronized (mCurrentTasks) {
			mCurrentTasks.add(callable);
		}
		callable.setTaskSerialNumber(mSerialNumber.incrementAndGet());
		mExecutorService.submit(callable);
	}

	@Override
	public void shutdown() {
		mExecutorService.shutdown();
	}

	@Override
	public void finish(Event<?> task) {
		synchronized (mCurrentTasks) {
			mCurrentTasks.remove(task);
			if (HttpGlobalConfiguration.debug) {
				Log.i(TAG, "[Task<number:" + task.getTaskSerialNumber()
						+ ">] be removed");
			}
		}
	}

	@Override
	public void cancel(final Object tag) {
		if (tag == null) {
			return;
		}
		cancel(new EventFilter() {

			@Override
			public boolean apply(Event<?> task) {
				return task.getTag().equals(tag);
			}
		});
	}

	@Override
	public void cancel(EventFilter mFilter) {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				if (mFilter.apply(task)) {
					task.cancel();
				}
			}
		}
	}

	@Override
	public void cancelAll() {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				task.cancel();
			}
		}
	}

	@Override
	public void cancel(final int serialNum) {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				if (task.getTaskSerialNumber() == serialNum) {
					task.cancel();
					break;
				}
			}
		}
	}

	public static class BsinceFutureTask<T> extends
			FutureTask<ResponseResult<T>> implements Comparable<Event<T>> {

		private final Event<T> mEvent;

		public BsinceFutureTask(Event<T> event) {
			super(event);
			this.mEvent = event;
		}

		@Override
		public int compareTo(Event<T> another) {
			return mEvent.compareTo(another);
		}

	}

	public static final class BsinceExecutorService extends ThreadPoolExecutor {

		public BsinceExecutorService(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <T> Future<T> submit(Callable<T> task) {
			if (task == null)
				throw new NullPointerException();

			BsinceFutureTask<T> ftask = new BsinceFutureTask<>((Event) task);
			execute(ftask);
			return (Future<T>) ftask;
		}

	}

	@Override
	public int ofEventCount() {
		return mCurrentTasks.size();
	}

}
