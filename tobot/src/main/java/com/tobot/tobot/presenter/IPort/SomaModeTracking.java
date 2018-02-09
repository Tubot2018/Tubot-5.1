package com.tobot.tobot.presenter.IPort;

/**
 * Created by Javen on 2018/2/2.
 */

public interface SomaModeTracking {
    void Enter(int sense);
    void Quit(int sense);
    void Feedback(byte[] feedback);
}
