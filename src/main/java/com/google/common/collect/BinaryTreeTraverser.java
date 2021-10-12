package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Iterator;

@Beta
@GwtCompatible
public abstract class BinaryTreeTraverser<T> extends TreeTraverser<T> {
   public abstract Optional<T> leftChild(T var1);

   public abstract Optional<T> rightChild(T var1);

   public final Iterable<T> children(final T root) {
      Preconditions.checkNotNull(root);
      return new FluentIterable<T>() {
         public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
               boolean doneLeft;
               boolean doneRight;

               protected T computeNext() {
                  Optional right;
                  if (!this.doneLeft) {
                     this.doneLeft = true;
                     right = BinaryTreeTraverser.this.leftChild(root);
                     if (right.isPresent()) {
                        return right.get();
                     }
                  }

                  if (!this.doneRight) {
                     this.doneRight = true;
                     right = BinaryTreeTraverser.this.rightChild(root);
                     if (right.isPresent()) {
                        return right.get();
                     }
                  }

                  return this.endOfData();
               }
            };
         }
      };
   }

   UnmodifiableIterator<T> preOrderIterator(T root) {
      return new BinaryTreeTraverser.PreOrderIterator(root);
   }

   UnmodifiableIterator<T> postOrderIterator(T root) {
      return new BinaryTreeTraverser.PostOrderIterator(root);
   }

   public final FluentIterable<T> inOrderTraversal(final T root) {
      Preconditions.checkNotNull(root);
      return new FluentIterable<T>() {
         public UnmodifiableIterator<T> iterator() {
            return BinaryTreeTraverser.this.new InOrderIterator(root);
         }
      };
   }

   private static <T> void pushIfPresent(Deque<T> stack, Optional<T> node) {
      if (node.isPresent()) {
         stack.addLast(node.get());
      }

   }

   private final class InOrderIterator extends AbstractIterator<T> {
      private final Deque<T> stack = new ArrayDeque(8);
      private final BitSet hasExpandedLeft = new BitSet();

      InOrderIterator(T root) {
         this.stack.addLast(root);
      }

      protected T computeNext() {
         while(!this.stack.isEmpty()) {
            T node = this.stack.getLast();
            if (this.hasExpandedLeft.get(this.stack.size() - 1)) {
               this.stack.removeLast();
               this.hasExpandedLeft.clear(this.stack.size());
               BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.rightChild(node));
               return node;
            }

            this.hasExpandedLeft.set(this.stack.size() - 1);
            BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.leftChild(node));
         }

         return this.endOfData();
      }
   }

   private final class PostOrderIterator extends UnmodifiableIterator<T> {
      private final Deque<T> stack = new ArrayDeque(8);
      private final BitSet hasExpanded;

      PostOrderIterator(T root) {
         this.stack.addLast(root);
         this.hasExpanded = new BitSet();
      }

      public boolean hasNext() {
         return !this.stack.isEmpty();
      }

      public T next() {
         while(true) {
            T node = this.stack.getLast();
            boolean expandedNode = this.hasExpanded.get(this.stack.size() - 1);
            if (expandedNode) {
               this.stack.removeLast();
               this.hasExpanded.clear(this.stack.size());
               return node;
            }

            this.hasExpanded.set(this.stack.size() - 1);
            BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.rightChild(node));
            BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.leftChild(node));
         }
      }
   }

   private final class PreOrderIterator extends UnmodifiableIterator<T> implements PeekingIterator<T> {
      private final Deque<T> stack = new ArrayDeque(8);

      PreOrderIterator(T root) {
         this.stack.addLast(root);
      }

      public boolean hasNext() {
         return !this.stack.isEmpty();
      }

      public T next() {
         T result = this.stack.removeLast();
         BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.rightChild(result));
         BinaryTreeTraverser.pushIfPresent(this.stack, BinaryTreeTraverser.this.leftChild(result));
         return result;
      }

      public T peek() {
         return this.stack.getLast();
      }
   }
}
