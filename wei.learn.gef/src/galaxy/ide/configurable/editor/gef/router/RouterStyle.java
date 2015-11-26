package galaxy.ide.configurable.editor.gef.router;

/**
 * @author caiyu
 * @date 2014-5-15
 */
public interface RouterStyle
{
    /**
     * ������
     */
    int TEST = 1 << 1;
    /**
     * �򻯹��ߵ�
     */
    int FLOYD_SIMPLIFY = 1 << 2;
    /**
     * ��������ƽ������, �򻯹��ߵ㲢��ʹ�ڵ�ƽ��
     */
    int FLOYD = 1 << 3;
    /**
     * Ѱ·ֻ�ο��ĸ�����
     */
    int FOUR_DIR = 1 << 4;

    /**
     * ͨ����ͬ��ɫչʾԤ���������Լ��Ѷ�
     */
    int SHOW_POOL = 1 << 5;
    
	/**
	 * �ڿ���̨��ʾ������Ϣ
	 */
	int CONSOLE_INFO = 1 << 6;

    int NONE = 0;

}
