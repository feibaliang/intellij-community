/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vcs.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.changes.local.ChangeListCommand;
import com.intellij.util.EventDispatcher;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class DelayedNotificator {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.DelayedNotificator");
  private final EventDispatcher<ChangeListListener> myDispatcher;
  // this is THE SAME service as is used for change list manager update (i.e. one thread for both processes)
  private final AtomicReference<ScheduledExecutorService> myService;
  private final MyProxyDispatcher myProxyDispatcher;

  public DelayedNotificator(EventDispatcher<ChangeListListener> dispatcher, final AtomicReference<ScheduledExecutorService> service) {
    myDispatcher = dispatcher;
    myService = service;
    myProxyDispatcher = new MyProxyDispatcher();
  }

  public void callNotify(final ChangeListCommand command) {
    myService.get().execute(new Runnable() {
      public void run() {
        try {
          command.doNotify(myDispatcher);
        }
        catch (Throwable e) {
          LOG.error(e);
        }
      }
    });
  }

  public ChangeListListener getProxyDispatcher() {
    return myProxyDispatcher;                                                                                
  }

  private class MyProxyDispatcher implements ChangeListListener {
    public void changeListAdded(final ChangeList list) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListAdded(list);
        }
      });
    }

    public void changesRemoved(final Collection<Change> changes, final ChangeList fromList) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changesRemoved(changes, fromList);
        }
      });
    }

    public void changesAdded(final Collection<Change> changes, final ChangeList toList) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changesAdded(changes, toList);
        }
      });
    }

    public void changeListRemoved(final ChangeList list) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListRemoved(list);
        }
      });
    }

    public void changeListChanged(final ChangeList list) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListChanged(list);
        }
      });
    }

    public void changeListRenamed(final ChangeList list, final String oldName) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListRenamed(list, oldName);
        }
      });
    }

    public void changeListCommentChanged(final ChangeList list, final String oldComment) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListCommentChanged(list, oldComment);
        }
      });
    }

    public void changesMoved(final Collection<Change> changes, final ChangeList fromList, final ChangeList toList) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changesMoved(changes, fromList, toList);
        }
      });
    }

    public void defaultListChanged(final ChangeList oldDefaultList, final ChangeList newDefaultList) {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().defaultListChanged(oldDefaultList, newDefaultList);
        }
      });
    }

    public void unchangedFileStatusChanged() {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().unchangedFileStatusChanged();
        }
      });
    }

    public void changeListUpdateDone() {
      myService.get().execute(new Runnable() {
        public void run() {
          myDispatcher.getMulticaster().changeListUpdateDone();
        }
      });
    }
  }
}
