package ru.govno.client.soundengine.filters;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import ru.govno.client.soundengine.filters.FilterException;

public abstract class BaseFilter {
    protected boolean isLoaded = false;
    protected boolean isEnabled = false;
    protected int id = -1;
    protected int slot = -1;

    public abstract void loadFilter();

    public abstract void checkParameters();

    public abstract void loadParameters();

    private static int safeID(BaseFilter filter) {
        return filter != null && filter.isEnabled && filter.id != -1 ? filter.id : 0;
    }

    private static int safeSlot(BaseFilter filter) {
        return filter != null && filter.isEnabled && filter.slot != -1 ? filter.slot : 0;
    }

    public static void load3SourceFilters(int sourceChannel, int type2, BaseFilter filter1, BaseFilter filter2, BaseFilter filter3) throws FilterException {
        AL11.alSource3i((int)sourceChannel, (int)type2, (int)BaseFilter.safeSlot(filter1), (int)BaseFilter.safeSlot(filter2), (int)BaseFilter.safeSlot(filter3));
        if (BaseFilter.checkError("load3SourceFilters attempt 1") != 0) {
            if (filter1 != null) {
                filter1.isLoaded = false;
                filter1.loadFilter();
            }
            if (filter2 != null) {
                filter2.isLoaded = false;
                filter2.loadFilter();
            }
            if (filter3 != null) {
                filter3.isLoaded = false;
                filter3.loadFilter();
            }
            if (BaseFilter.checkError("load3SourceFilters attempt 2") != 0) {
                throw new FilterException("Sound Filters - Error while trying to load 3 source filters.");
            }
        }
    }

    public static void loadSourceFilter(int sourceChannel, int type2, BaseFilter filter) throws FilterException {
        AL10.alSourcei((int)sourceChannel, (int)type2, (int)BaseFilter.safeSlot(filter));
        if (BaseFilter.checkError("loadSourceFilter attempt 1") != 0) {
            if (filter != null) {
                filter.isLoaded = false;
                filter.loadFilter();
            }
            AL10.alSourcei((int)sourceChannel, (int)type2, (int)BaseFilter.safeSlot(filter));
            if (BaseFilter.checkError("loadSourceFilter attempt 2") != 0) {
                throw new FilterException("Sound Filters - Error while trying to load source filter.");
            }
        }
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    private static int checkError(String location) {
        int err = AL10.alGetError();
        if (err != 0) {
            System.out.println("[Sound Filters] Caught AL error in '" + location + "'! Error is " + err);
            return err;
        }
        return 0;
    }
}

