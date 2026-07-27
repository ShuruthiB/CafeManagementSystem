export interface OrderItem {
  menuItemName: string;
  quantity: number;
  priceAtOrderTime: number;
}

export type OrderStatus = 'PLACED' | 'ACCEPTED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';

export interface Order {
  id: number;
  customerName: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  handledByName: string | null;
  items: OrderItem[];
}
