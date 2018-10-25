public class VirtualPageMappingToPhysicalPageRecord {

    private final Integer id;
    private Boolean precedenceBit;
    private Boolean modificationBit;
    private Boolean readBit;
    // bit of page calling (R)
    private Boolean referencedBit;
    private PhysicalPage physicalPage;
    private Integer lastAccessedTime;

    public VirtualPageMappingToPhysicalPageRecord(Integer id) {
        this.id = id;
        this.precedenceBit = false;
        this.modificationBit = false;
        this.readBit = false;
        this.referencedBit = false;
        this.physicalPage = null;
        this.lastAccessedTime = 0;
    }

    public Integer getId() {
        return id;
    }

    public Boolean getPrecedenceBit() {
        return precedenceBit;
    }

    public void setPrecedenceBit(Boolean precedenceBit) {
        this.precedenceBit = precedenceBit;
    }

    public Boolean getModificationBit() {
        return modificationBit;
    }

    public void setModificationBit(Boolean modificationBit) {
        this.modificationBit = modificationBit;
    }

    public Boolean getReadBit() {
        return readBit;
    }

    public void setReadBit(Boolean readBit) {
        this.readBit = readBit;
    }

    public Boolean getReferencedBit() {
        return referencedBit;
    }

    public void setReferencedBit(Boolean referencedBit) {
        this.referencedBit = referencedBit;
    }

    public PhysicalPage getPhysicalPage() {
        return physicalPage;
    }

    public void setPhysicalPage(PhysicalPage physicalPage) {
        this.physicalPage = physicalPage;
    }

    public Integer getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(Integer lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public String toString() {
        return "VirtualPageMappingToPhysicalPageRecord{" +
                "id=" + id +
                ", precedenceBit=" + precedenceBit +
                ", modificationBit=" + modificationBit +
                ", readBit=" + readBit +
                ", referencedBit=" + referencedBit +
                ", physicalPage=" + physicalPage +
                ", lastAccessedTime=" + lastAccessedTime +
                '}';
    }
}
