package fu.hao.trust.dvm;

import patdroid.dalvik.Instruction;

/**
 * @ClassName: ByteCode
 * @Description: ʵ�ֺ���ָ��
 * @author: Hao Fu
 * @date: Feburary. 13, 2016 10:25:42 AM
 */
public interface ByteCode {
    void func(DalvikVM vm, Instruction inst);
}
