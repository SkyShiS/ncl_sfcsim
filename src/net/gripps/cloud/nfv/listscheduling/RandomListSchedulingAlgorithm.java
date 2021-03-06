package net.gripps.cloud.nfv.listscheduling;

import net.gripps.cloud.core.CloudEnvironment;
import net.gripps.cloud.nfv.NFVUtil;
import net.gripps.cloud.nfv.sfc.BaseVNFSchedulingAlgorithm;
import net.gripps.cloud.nfv.sfc.SFC;
import net.gripps.cloud.nfv.sfc.VNF;

import java.util.Iterator;

/**
 * Created by Hidehiro Kanemitsu on 2018/12/19
 */
public class RandomListSchedulingAlgorithm extends BaseVNFSchedulingAlgorithm {
    public RandomListSchedulingAlgorithm(CloudEnvironment env, SFC sfc) {
        super(env, sfc);
    }

    /**
     *
     * @return
     */
    public VNF selectVNF() {
        //Freeリストから選択する．
        long size = this.freeVNFSet.getList().size();
        //乱数により，VNFを選択する．
        long idx = 0;
        Iterator<Long> idIte = this.freeVNFSet.iterator();
        //乱数を決定させる．
        long location = NFVUtil.genLong(0, size - 1);
        Long retID = -1L;
        while (idIte.hasNext()) {
            Long id = idIte.next();
            if (idx == location) {
                retID = id;
            }
            idx++;
        }
        //SFCから，指定IDのVNFを取得する．
        VNF selectedVNF = this.sfc.findVNFByLastID(retID);
        //VNFをスケジュールする．これは，親クラスであるAbstractFairSchedulingAlgorithmのscheduleVNFメソッド
        //をcallしており，fairnessに基づいて割り当てている．
        //this.scheduleVNF(selectedVNF, this.env.getGlobal_vcpuMap());

        return selectedVNF;
    }

    public void mainProcess() {
        //未スケジュールなVNFが残っている間，行うループ
        while (!this.getUnScheduledVNFSet().isEmpty()) {
            VNF vnf = this.selectVNF();
            if(vnf == null){
                System.out.println("test");
            }
            //vcpu全体から，vnfの割当先を選択する．
            this.scheduleVNF(vnf, this.vcpuMap);
        }
        double val = -1;
        Iterator<Long> endITe = this.getSfc().getEndVNFSet().iterator();
        while (endITe.hasNext()) {
            Long eID = endITe.next();
            VNF endVNF = this.sfc.findVNFByLastID(eID);
            if (endVNF.getFinishTime() >= val) {
                val = endVNF.getFinishTime();
            }
        }
        //応答時間を決める．
        this.makeSpan = val;
    }
}
