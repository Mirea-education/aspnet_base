public class Main {
    static long total(long unitPrice, int quantity) {
        if (unitPrice < 0 || quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Price or quantity is invalid");
        }
        return Math.multiplyExact(unitPrice, quantity);
    }

    public static void main(String[] args) {
        int quantity = args.length == 0 ? 2 : Integer.parseInt(args[0]);
        long gross = total(129_900L, quantity);
        // Учебное правило — 5 процентов при покупке от двух единиц.
        long discount = quantity >= 2 ? gross * 5 / 100 : 0;
        System.out.println("GROSS_CENTS=" + gross);
        System.out.println("DISCOUNT_CENTS=" + discount);
        System.out.println("PAY_CENTS=" + (gross - discount));
        int[] quantities = {2, 1, 3};
        int count = 0;
        for (int value : quantities) count += value;
        System.out.println("ITEMS=" + count);
    }
}
