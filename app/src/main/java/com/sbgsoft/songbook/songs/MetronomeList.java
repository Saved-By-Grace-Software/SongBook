package com.sbgsoft.songbook.songs;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by SamIAm on 1/5/2016.
 */
public class MetronomeList {

    //region Private Class Members
    private MetronomeNode start;
    private MetronomeNode end;
    private MetronomeNode currentNode;
    private Activity mActivity;
    private boolean firstTick;
    private int imageOn = -1;
    private int imageOff = -1;
    //endregion

    //region Public Class Members
    public int size;
    //endregion

    //region Constructor
    public MetronomeList(Activity _activity) {
        start = null;
        end = null;
        currentNode = start;
        size = 0;
        mActivity = _activity;
        firstTick = false;
    }
    //endregion

    //region Public Functions
    // Makes the metronome tick
    public void tick() {
        // Special case with only a single dot
        if (size == 1) {
            // Initialize currentNode if it isn't already
            if (currentNode == null) {
                currentNode = start;
            }

            // Turn the image on
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentNode.getData().setImageResource(imageOn);
                }
            });

            // Wait for 100ms
            try {
                Thread.sleep(150);
            } catch (InterruptedException ie) { }

            // Turn the image back off
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentNode.getData().setImageResource(imageOff);
                }
            });
        }
        // Multiple dots to tick
        else if (size > 1) {

            // Initialize currentNode if it isn't already
            if (currentNode == null) {
                currentNode = start;
                firstTick = true;
            }

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Check for first tick
                    if (firstTick) {
                        currentNode.getData().setImageResource(imageOn);
                        firstTick = false;
                    } else {
                        // Reset the current node
                        currentNode.getData().setImageResource(imageOff);

                        if (currentNode.hasNext()) {
                            // Tick the next node
                            currentNode.getNext().getData().setImageResource(imageOn);

                            // Update current node to the next node
                            currentNode = currentNode.getNext();
                        }
                    }
                }
            });
        }
    }

    // Appends the specified data to the linked list
    public void add(ImageView data) {
        // Create the new node to add
        MetronomeNode temp = new MetronomeNode(data);
        temp.setNext(start);

        // Check for an empty list
        if (start == null) {
            // Empty list, make the new node the start
            start = temp;
            end = start;
        } else {
            end.setNext(temp);
            end = temp;
        }

        // Increment the list counter
        size++;
    }

    // Removed the specified node at the specified position
    public void removeAtPos(int pos) {
        if (size == 1 && pos == 1)
        {
            start = null;
            end = null;
            size = 0;
            return ;
        }
        if (pos == 1)
        {
            start = start.getNext();
            end.setNext(start);
            size--;
            return ;
        }
        if (pos == size)
        {
            MetronomeNode s = start;
            MetronomeNode t = start;
            while (s != end)
            {
                t = s;
                s = s.getNext();
            }
            end = t;
            end.setNext(start);
            size --;
            return;
        }
        MetronomeNode ptr = start;
        pos = pos - 1 ;
        for (int i = 1; i < size - 1; i++)
        {
            if (i == pos)
            {
                MetronomeNode tmp = ptr.getNext();
                tmp = tmp.getNext();
                ptr.setNext(tmp);
                break;
            }
            ptr = ptr.getNext();
        }
        size-- ;
    }

    // Sets the on image id
    public void setImageOn(int _imageOn) {
        imageOn = _imageOn;
    }

    // Sets the off image id
    public void setImageOff(int _imageOff) {
        imageOff = _imageOff;
    }

    // Hides all nodes in the list
    public void hideAllIcons() {
        MetronomeNode current = start;

        if (current != null) {
            do {
                // Hide the current node
                current.getData().setVisibility(View.GONE);

                // Move to the next node
                current = current.getNext();
            } while (current != start);
        }
    }

    // Resets the current node back to the start
    public void resetToStart() {
        // Set the current node back to the start
        currentNode = start;

        // Set the start image to off if we only have one dot
        if (size == 1) {
            // Set the start image on
            start.getData().setImageResource(imageOff);
        } else {
            if (start != null) {
                // Set the start image on
                start.getData().setImageResource(imageOn);

                // Loop through the rest of the dots and set them off
                MetronomeNode curr = start.getNext();
                while (curr != null && curr != start) {
                    // Set the image off
                    curr.getData().setImageResource(imageOff);

                    // Go to the next icon
                    curr = curr.getNext();
                }
            }
        }
    }
    //endregion

    //region Metronome Node Class
    /**
     * Node class for the Metronome Linked List
     */
    public class MetronomeNode {
        private MetronomeNode next;
        private ImageView data;

        public MetronomeNode(ImageView _data) {
            next = null;
            data = _data;
        }

        public MetronomeNode(ImageView _data, MetronomeNode _next) {
            next = _next;
            data = _data;
        }

        /**
         * Determines if this node has a next node
         * @return True if has next, false if no next
         */
        public boolean hasNext() {
            return (next != null);
        }

        /**
         * Gets the next node
         * @return The next node
         */
        public MetronomeNode getNext() {
            return next;
        }

        /**
         * Sets the next node
         * @param _next The next node
         */
        public void setNext(MetronomeNode _next) {
            next = _next;
        }

        /**
         * Gets the data of the node
         * @return The node data
         */
        public ImageView getData() {
            return data;
        }

        /**
         * Sets the data for the node
         * @param _data The data to set for the node
         */
        public void setData(ImageView _data) {
            data = _data;
        }
    }
    //endregion
}

