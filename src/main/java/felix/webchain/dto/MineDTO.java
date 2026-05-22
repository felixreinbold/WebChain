package felix.webchain.dto;

public class MineDTO {
    private String minerAddress;

    public MineDTO() {}

    public MineDTO(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }
}