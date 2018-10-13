public class VirtualPageMappingToPhysicalPageRecord {

    private Boolean precedenceBit;
    private Boolean modificationBit;
    private Boolean readBit;
    // bit of page calling
    private Boolean referencedBit;
    private PhysicalPage physicalPage;

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
}
