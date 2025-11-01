package mage.game.events;

public class PhasedOutBatchEvent extends BatchEvent<GameEvent> {

    public PhasedOutBatchEvent() {
        super(EventType.PHASED_OUT_BATCH);
    }
}
