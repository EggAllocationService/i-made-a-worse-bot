package io.egg.badidea.mixing;

public class AutoClosingAudioStream extends CabotAudioStream {
    public boolean alwaysPriority = false;

    public AutoClosingAudioStream(int a) {
        super(a);
    }

    @Override
    public void push(short[] audio) {
        super.push(audio);
        setOpen(true);
        setPriority(alwaysPriority);
    }

    @Override
    public boolean canProvideFrame() {
        var result = super.canProvideFrame();
        if (!result) {
            setOpen(false);
            setPriority(false);
        }
        return result;
    }
    
}
