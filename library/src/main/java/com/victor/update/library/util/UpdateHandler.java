package com.victor.update.library.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedHashSet;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2028, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: MainHandler.java
 * Author: Victor
 * Date: 2019/3/13 10:11
 * Description: 主线程Handler
 * -----------------------------------------------------------------
 */

public class UpdateHandler extends Handler {

    private static LinkedHashSet<OnMainHandlerImpl> mainHandlers = new LinkedHashSet<>();

    private static UpdateHandler sMainHandler;

    private UpdateHandler() {
        super(Looper.getMainLooper());
    }

    public static UpdateHandler get() {
        if (sMainHandler == null) {
            sMainHandler = new UpdateHandler();
        }
        return sMainHandler;
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        for (OnMainHandlerImpl onMainHandlerImpl : mainHandlers) {
            if (onMainHandlerImpl != null) {
                onMainHandlerImpl.handleMainMessage(message);
            }
        }
    }

    public UpdateHandler register(OnMainHandlerImpl onMainHandlerImpl) {
        if (onMainHandlerImpl != null) {
            mainHandlers.add(onMainHandlerImpl);
        }
        return this;
    }

    public void unregister(OnMainHandlerImpl onMainHandlerImpl) {
        if (onMainHandlerImpl != null) {
            mainHandlers.remove(onMainHandlerImpl);
        }
    }

    public static void clear() {
        mainHandlers.clear();
        if (sMainHandler != null) {
            sMainHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 主线程执行
     *
     * @param runnable
     */
    public static void runMainThread(Runnable runnable) {
        get().post(runnable);
    }

    public interface OnMainHandlerImpl {
        void handleMainMessage(Message message);
    }
}
