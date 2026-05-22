package felix.webchain.service;

import felix.webchain.model.Block;

public class BlockService {


    public void mineBlock(Block block, int difficulty){

        String target = "";
        for(int i = 0; i<difficulty; i++){
            target+="0";
        }

        System.out.println("Mining Block "+block.getIndex()+"...");

        long startTime = System.currentTimeMillis();

        while(!block.getHash().startsWith(target)){
            int nonce = block.getNonce();
            block.setNonce(nonce+1);
            block.calculateHash();

        }

        long endTime = System.currentTimeMillis();
        System.out.println("Block "+block.getIndex()+" geminded!");
        System.out.println("Nonce"+block.getIndex()+block.getNonce());
        System.out.println("Hash"+block.getHash());
        System.out.println("Zeit: "+(endTime-startTime)+" ms\n");
    }


}
