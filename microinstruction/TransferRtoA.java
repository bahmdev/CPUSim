/**
 * Author: Jinghui Yu
 * LastEditingDate: 6/7/2013
 */

package cpusim.microinstruction;

import cpusim.ExecutionException;
import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.Assert;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The TransferRtoA microinstruction transfers data from a register to a register array.
 */
public class TransferRtoA extends Microinstruction {
    private SimpleObjectProperty<Register> source;
    private SimpleIntegerProperty srcStartBit;
    private SimpleObjectProperty<RegisterArray> dest;
    private SimpleIntegerProperty destStartBit;
    private SimpleIntegerProperty numBits;
    private SimpleObjectProperty<Register> index;
    private SimpleIntegerProperty indexStart;
    private SimpleIntegerProperty indexNumBits;

    /**
     * Constructor
     * creates a new Test object with input values.
     *
     * @param name name of the microinstruction.
     * @param source the register whose value is to be tested.
     * @param srcStartBit an integer indicting the leftmost bit to be tested.
     * @param dest the destination register.
     * @param destStartBit an integer indicting the leftmost bit to be changed.
     * @param numBits a non-negative integer indicating the number of bits to be tested.
     */
    public TransferRtoA(String name,
                        Register source,
                        Integer srcStartBit,
                        RegisterArray dest,
                        Integer destStartBit,
                        Integer numBits,
                        Register index,
                        Integer indexStart,
                        Integer indexNumBits){
        super(name);
        this.source = new SimpleObjectProperty<>(source);
        this.srcStartBit = new SimpleIntegerProperty(srcStartBit);
        this.dest = new SimpleObjectProperty<>(dest);
        this.destStartBit = new SimpleIntegerProperty(destStartBit);
        this.numBits = new SimpleIntegerProperty(numBits);
        this.index = new SimpleObjectProperty<>(index);
        this.indexStart = new SimpleIntegerProperty(indexStart);
        this.indexNumBits = new SimpleIntegerProperty(indexNumBits);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public Register getSource(){
        return source.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newSource the new selected register for the set microinstruction.
     */
    public void setSource(Register newSource){
        source.set(newSource);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public Integer getSrcStartBit(){
        return srcStartBit.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newSrcStartBit the new index of the start bit for the set microinstruction.
     */
    public void setSrcStartBit(Integer newSrcStartBit){
        srcStartBit.set(newSrcStartBit);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public RegisterArray getDest(){
        return dest.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newDest the new selected register for the set microinstruction.
     */
    public void setDest(RegisterArray newDest){
        dest.set(newDest);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public Integer getDestStartBit(){
        return destStartBit.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newDestStartBit the new index of the start bit for the set microinstruction.
     */
    public void setDestStartBit(Integer newDestStartBit){
        destStartBit.set(newDestStartBit);
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public Integer getNumBits(){
        return numBits.get();
    }

    /**
     * updates the number of bits of the value.
     *
     * @param newNumbits the new value of the number of bits.
     */
    public void setNumBits(Integer newNumbits){
        numBits.set(newNumbits);
    }

    /**
     * returns the name of the set microinstruction as a string.
     *
     * @return the name of the set microinstruction.
     */
    public Register getIndex(){
        return index.get();
    }

    /**
     * updates the register used by the microinstruction.
     *
     * @param newIndex the new selected register for the set microinstruction.
     */
    public void setIndex(Register newIndex){
        index.set(newIndex);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     *
     * @return the integer value of the index.
     */
    public Integer getIndexStart(){
        return indexStart.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     *
     * @param newIndexStart the new index of the start bit for the set microinstruction.
     */
    public void setIndexStart(Integer newIndexStart){
        indexStart.set(newIndexStart);
    }

    /**
     * returns the number of bits of the value.
     *
     * @return the integer value of the number of bits.
     */
    public Integer getIndexNumBits(){
        return indexNumBits.get();
    }

    /**
     * updates the number of bits of the value.
     *
     * @param newIndexNumBits the new value of the number of bits.
     */
    public void setIndexNumBits(Integer newIndexNumBits){
        indexNumBits.set(newIndexNumBits);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "transferRtoA";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     *
     * @return a copy of the Set class
     */
    public Object clone(){
        return new TransferRtoA(getName(),getSource(),getSrcStartBit(),
                getDest(),getDestStartBit(),getNumBits(),
                getIndex(),getIndexStart(),getIndexNumBits());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        Assert.That(oldMicro instanceof TransferRtoA,
                "Passed non-TransferRtoA to TransferRtoA.copyDataTo()");
        TransferRtoA newTransferRtoA = (TransferRtoA) oldMicro;
        newTransferRtoA.setName(getName());
        newTransferRtoA.setSource(getSource());
        newTransferRtoA.setSrcStartBit(getSrcStartBit());
        newTransferRtoA.setDest(getDest());
        newTransferRtoA.setIndex(getIndex());
        newTransferRtoA.setDestStartBit(getDestStartBit());
        newTransferRtoA.setNumBits(getNumBits());
        newTransferRtoA.setIndexStart(getIndexStart());
        newTransferRtoA.setIndexNumBits(getIndexNumBits());
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        long indexValue = index.get().getValue();
        indexValue = (indexValue << (64 - index.get().getWidth() + indexStart.get()))
                >>> (64 - indexNumBits.get());
        //if indexValue is negative or too large, throw an exception.
        if (indexValue < 0 || indexValue >= dest.get().getLength())
            throw new ExecutionException("Index value: " + indexValue +
                    " is out of range for choosing a register\nfrom array: " +
                    dest.get() + " in the transferRtoA microinstruction: " +
                    getName());
        Register destination = dest.get().registers().get((int) indexValue);
        int sourceWidth = source.get().getWidth();
        int destWidth = destination.getWidth();
        long sourceValue = source.get().getValue() << (64 - sourceWidth);
        long destValue = destination.getValue() << (64 - destWidth);
        long leftDestPart =
                (destStartBit.get() == 0 ? 0 :
                        (destValue >>> (64 - destStartBit.get()))
                                << (64 - destStartBit.get()));
        long rightDestPart =
                (destStartBit.get() + numBits.get() == 64 ? 0 :
                        (destValue << (destStartBit.get() + numBits.get()))
                                >>> (destStartBit.get() + numBits.get()));
        long middlePart = (((sourceValue << srcStartBit.get())
                >>> (64 - numBits.get()))
                << (64 - numBits.get()))
                >>> destStartBit.get();
        long result = leftDestPart | middlePart | rightDestPart;
        result = result >> (64 - destWidth);
        destination.setValue(result);
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription(){
        return "<TransferRtoA name=\"" + getHTMLName() +
                "\" source=\"" + getSource().getID() +
                "\" srcStartBit=\"" + getSrcStartBit() +
                "\" dest=\"" + getDest().getID() +
                "\" destStartBit=\"" + getDestStartBit() +
                "\" numBits=\"" + getNumBits() +
                "\" index=\"" + getIndex().getID() +
                "\" indexStart=\"" + getIndexStart() +
                "\" indexNumBits=\"" + getIndexNumBits() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() +
                "</TD><TD>" + getSource().getHTMLName() +
                "</TD><TD>" + getSrcStartBit() +
                "</TD><TD>" + getDest().getHTMLName() +
                "</TD><TD>" + getDestStartBit() +
                "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getIndex().getHTMLName() +
                "</TD><TD>" + getIndexStart() +
                "</TD><TD>" + getIndexNumBits() +
                "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m){
        return (m == source.get() || m == dest.get() || m == index.get());
    }

}
