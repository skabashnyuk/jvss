/*
 * Copyright 2009 HPDI, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvss.logical;

/**
 * Base class for VSS revision action descriptions.
 */
public abstract class VssAction
{

   /**
    * Enumeration of logical VSS revision actions.
    */
   public enum VssActionType {
      Label, Create, Destroy, Add, Delete, Recover, Rename, MoveFrom, MoveTo, Share, Pin, Branch, Edit, Archive, Restore
   }

   /**
    * Represents a VSS project/file add action.
    * 
    */
   public static class VssAddAction extends VssNamedAction
   {
      /**
       * @param name
       */
      public VssAddAction(VssItemName name)
      {
         super(name);
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Add %s", name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Add;
      }
   }

   /**
    * Represents a VSS archive action.
    * 
    */
   public static class VssArchiveAction extends VssNamedAction
   {
      private final String archivePath;

      public VssArchiveAction(VssItemName name, String archivePath)
      {
         super(name);
         this.archivePath = archivePath;
      }

      /**
       * @return the archivePath
       */
      public String getArchivePath()
      {
         return archivePath;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Archive %s to %s", name, archivePath);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Archive;
      }
   }

   /**
    * Represents a VSS file branch action.
    * 
    */
   public static class VssBranchAction extends VssNamedAction
   {
      private final VssItemName source;

      public VssBranchAction(VssItemName name, VssItemName source)
      {
         super(name);
         this.source = source;
      }

      /**
       * @return the source
       */
      public VssItemName getSource()
      {
         return source;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Branch %s from %s", name, source.getPhysicalName());
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Branch;
      }
   }

   /**
    * Represents a VSS project/file create action.
    * 
    */
   public static class VssCreateAction extends VssNamedAction
   {

      /**
       * @param name
       */
      public VssCreateAction(VssItemName name)
      {
         super(name);
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Create %s", name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Create;
      }

   }

   /**
    * Represents a VSS project/file delete action.
    * 
    */
   public static class VssDeleteAction extends VssNamedAction
   {
      /**
       * @param name
       */
      public VssDeleteAction(VssItemName name)
      {
         super(name);
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Delete %s", name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Delete;
      }
   }

   /**
    * Represents a VSS project/file destroy action.
    * 
    */
   public static class VssDestroyAction extends VssNamedAction
   {

      /**
       * @param name
       */
      public VssDestroyAction(VssItemName name)
      {
         super(name);
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Destroy %s", name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Destroy;
      }
   }

   /**
    * Represents a VSS file edit action.
    * 
    */
   public static class VssEditAction extends VssAction
   {
      private final String physicalName;

      public VssEditAction(String physicalName)
      {
         this.physicalName = physicalName;
      }

      /**
       * @return the physicalName
       */
      public String getPhysicalName()
      {
         return physicalName;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return "Edit " + physicalName;
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Edit;
      }
   }

   /**
    * Represents a VSS label action.
    */
   public static class VssLabelAction extends VssAction
   {

      private final String label;

      /**
       * @param label
       */
      public VssLabelAction(String label)
      {
         super();
         this.label = label;
      }

      /**
       * @return the label
       */
      public String getLabel()
      {
         return label;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return "Label " + label;
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Label;
      }
   }

   /**
    * Represents a VSS project move-from action.
    * 
    */
   public static class VssMoveFromAction extends VssNamedAction
   {

      private final String originalProject;

      public VssMoveFromAction(VssItemName name, String originalProject)
      {
         super(name);
         this.originalProject = originalProject;
      }

      /**
       * @return the originalProject
       */
      public String getOriginalProject()
      {
         return originalProject;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Move %s from %s", name, originalProject);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.MoveFrom;
      }
   }

   /**
    * Represents a VSS project move-to action.
    * 
    */
   public static class VssMoveToAction extends VssNamedAction
   {
      private final String newProject;

      public VssMoveToAction(VssItemName name, String newProject)
      {
         super(name);
         this.newProject = newProject;
      }

      /**
       * @return the newProject
       */
      public String getNewProject()
      {
         return newProject;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Move %s to %s", name, newProject);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.MoveTo;
      }
   }

   /**
    * Base class for VSS project actions that target a particular item.
    * 
    */
   public static abstract class VssNamedAction extends VssAction
   {
      protected final VssItemName name;

      public VssNamedAction(VssItemName name)
      {
         this.name = name;
      }

      public VssItemName name()
      {
         return name;
      }
   }

   /**
    * Represents a VSS file pin/unpin action.
    * 
    */
   public static class VssPinAction extends VssNamedAction
   {
      private final boolean pinned;

      private final int revision;

      public VssPinAction(VssItemName name, boolean pinned, int revision)
      {
         super(name);
         this.pinned = pinned;
         this.revision = revision;
      }

      /**
       * @return the revision
       */
      public int getRevision()
      {
         return revision;
      }

      /**
       * @return the pinned
       */
      public boolean isPinned()
      {
         return pinned;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("%s %s at revision {2}", pinned ? "Pin " : "Unpin ", name, revision);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Pin;
      }
   }

   /**
    * Represents a VSS project/file recover action.
    * 
    */
   public static class VssRecoverAction extends VssNamedAction
   {
      /**
       * @param name
       */
      public VssRecoverAction(VssItemName name)
      {
         super(name);
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Recover %s", name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Recover;
      }
   }

   /**
    * Represents a VSS project/file rename action.
    * 
    */
   public static class VssRenameAction extends VssNamedAction
   {

      private final String originalName;

      /**
       * @param name
       */
      public VssRenameAction(VssItemName name, String originalName)
      {
         super(name);
         this.originalName = originalName;
      }

      /**
       * @return the originalName
       */
      public String getOriginalName()
      {
         return originalName;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Rename %s to %s", originalName, name);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Rename;
      }
   }

   /**
    * Represents a VSS restore from archive action.
    * 
    */
   public static class VssRestoreAction extends VssNamedAction
   {
      private final String archivePath;

      public VssRestoreAction(VssItemName name, String archivePath)
      {
         super(name);
         this.archivePath = archivePath;
      }

      /**
       * @return the archivePath
       */
      public String getArchivePath()
      {
         return archivePath;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Restore %s from archive %s", name, archivePath);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Restore;
      }
   }

   /**
    * Represents a VSS file share action.
    * 
    */
   public static class VssShareAction extends VssNamedAction
   {
      private final String originalProject;

      public VssShareAction(VssItemName name, String originalProject)
      {
         super(name);
         this.originalProject = originalProject;
      }

      public String getOriginalProject()
      {
         return originalProject;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return String.format("Share %s from %s", name, originalProject);
      }

      /**
       * @see org.jvss.logical.VssAction#type()
       */
      @Override
      public VssActionType type()
      {
         return VssActionType.Share;
      }
   }

   public abstract VssActionType type();
}
