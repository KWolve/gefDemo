package galaxy.ide.configurable.editor.gef.router;

/**
 * �ڵ�ȼ���REֱ�Ǳߣ�BEб�Ǳ�
 * 
 * @author caiyu
 * @date 2014-5-15
 */
public enum ANodeLevel {
    EASY(10, 14), NORMAL(20, 28), HARD(300,520), DEAD(2000, 2800);
    /**
     * ֱ�Ǳ�
     */
    public final int RE;
    /**
     * б�Ǳ�
     */
    public final int BE;

    ANodeLevel(int RE, int BE) {
        this.RE = RE;
        this.BE = BE;
    }
}
