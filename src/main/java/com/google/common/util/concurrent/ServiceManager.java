package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

@Beta
@GwtIncompatible
public final class ServiceManager {
   private static final Logger logger = Logger.getLogger(ServiceManager.class.getName());
   private static final ListenerCallQueue.Callback<ServiceManager.Listener> HEALTHY_CALLBACK = new ListenerCallQueue.Callback<ServiceManager.Listener>("healthy()") {
      void call(ServiceManager.Listener listener) {
         listener.healthy();
      }
   };
   private static final ListenerCallQueue.Callback<ServiceManager.Listener> STOPPED_CALLBACK = new ListenerCallQueue.Callback<ServiceManager.Listener>("stopped()") {
      void call(ServiceManager.Listener listener) {
         listener.stopped();
      }
   };
   private final ServiceManager.ServiceManagerState state;
   private final ImmutableList<Service> services;

   public ServiceManager(Iterable<? extends Service> services) {
      ImmutableList<Service> copy = ImmutableList.copyOf(services);
      if (copy.isEmpty()) {
         logger.log(Level.WARNING, "ServiceManager configured with no services.  Is your application configured properly?", new ServiceManager.EmptyServiceManagerWarning());
         copy = ImmutableList.of(new ServiceManager.NoOpService());
      }

      this.state = new ServiceManager.ServiceManagerState(copy);
      this.services = copy;
      WeakReference<ServiceManager.ServiceManagerState> stateReference = new WeakReference(this.state);
      Iterator i$ = copy.iterator();

      while(i$.hasNext()) {
         Service service = (Service)i$.next();
         service.addListener(new ServiceManager.ServiceListener(service, stateReference), MoreExecutors.directExecutor());
         Preconditions.checkArgument(service.state() == Service.State.NEW, "Can only manage NEW services, %s", (Object)service);
      }

      this.state.markReady();
   }

   public void addListener(ServiceManager.Listener listener, Executor executor) {
      this.state.addListener(listener, executor);
   }

   public void addListener(ServiceManager.Listener listener) {
      this.state.addListener(listener, MoreExecutors.directExecutor());
   }

   @CanIgnoreReturnValue
   public ServiceManager startAsync() {
      Iterator i$ = this.services.iterator();

      Service service;
      while(i$.hasNext()) {
         service = (Service)i$.next();
         Service.State state = service.state();
         Preconditions.checkState(state == Service.State.NEW, "Service %s is %s, cannot start it.", service, state);
      }

      i$ = this.services.iterator();

      while(i$.hasNext()) {
         service = (Service)i$.next();

         try {
            this.state.tryStartTiming(service);
            service.startAsync();
         } catch (IllegalStateException var4) {
            logger.log(Level.WARNING, "Unable to start Service " + service, var4);
         }
      }

      return this;
   }

   public void awaitHealthy() {
      this.state.awaitHealthy();
   }

   public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
      this.state.awaitHealthy(timeout, unit);
   }

   @CanIgnoreReturnValue
   public ServiceManager stopAsync() {
      Iterator i$ = this.services.iterator();

      while(i$.hasNext()) {
         Service service = (Service)i$.next();
         service.stopAsync();
      }

      return this;
   }

   public void awaitStopped() {
      this.state.awaitStopped();
   }

   public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
      this.state.awaitStopped(timeout, unit);
   }

   public boolean isHealthy() {
      Iterator i$ = this.services.iterator();

      Service service;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         service = (Service)i$.next();
      } while(service.isRunning());

      return false;
   }

   public ImmutableMultimap<Service.State, Service> servicesByState() {
      return this.state.servicesByState();
   }

   public ImmutableMap<Service, Long> startupTimes() {
      return this.state.startupTimes();
   }

   public String toString() {
      return MoreObjects.toStringHelper(ServiceManager.class).add("services", Collections2.filter(this.services, Predicates.not(Predicates.instanceOf(ServiceManager.NoOpService.class)))).toString();
   }

   private static final class EmptyServiceManagerWarning extends Throwable {
      private EmptyServiceManagerWarning() {
      }

      // $FF: synthetic method
      EmptyServiceManagerWarning(Object x0) {
         this();
      }
   }

   private static final class NoOpService extends AbstractService {
      private NoOpService() {
      }

      protected void doStart() {
         this.notifyStarted();
      }

      protected void doStop() {
         this.notifyStopped();
      }

      // $FF: synthetic method
      NoOpService(Object x0) {
         this();
      }
   }

   private static final class ServiceListener extends Service.Listener {
      final Service service;
      final WeakReference<ServiceManager.ServiceManagerState> state;

      ServiceListener(Service service, WeakReference<ServiceManager.ServiceManagerState> state) {
         this.service = service;
         this.state = state;
      }

      public void starting() {
         ServiceManager.ServiceManagerState state = (ServiceManager.ServiceManagerState)this.state.get();
         if (state != null) {
            state.transitionService(this.service, Service.State.NEW, Service.State.STARTING);
            if (!(this.service instanceof ServiceManager.NoOpService)) {
               ServiceManager.logger.log(Level.FINE, "Starting {0}.", this.service);
            }
         }

      }

      public void running() {
         ServiceManager.ServiceManagerState state = (ServiceManager.ServiceManagerState)this.state.get();
         if (state != null) {
            state.transitionService(this.service, Service.State.STARTING, Service.State.RUNNING);
         }

      }

      public void stopping(Service.State from) {
         ServiceManager.ServiceManagerState state = (ServiceManager.ServiceManagerState)this.state.get();
         if (state != null) {
            state.transitionService(this.service, from, Service.State.STOPPING);
         }

      }

      public void terminated(Service.State from) {
         ServiceManager.ServiceManagerState state = (ServiceManager.ServiceManagerState)this.state.get();
         if (state != null) {
            if (!(this.service instanceof ServiceManager.NoOpService)) {
               ServiceManager.logger.log(Level.FINE, "Service {0} has terminated. Previous state was: {1}", new Object[]{this.service, from});
            }

            state.transitionService(this.service, from, Service.State.TERMINATED);
         }

      }

      public void failed(Service.State from, Throwable failure) {
         ServiceManager.ServiceManagerState state = (ServiceManager.ServiceManagerState)this.state.get();
         if (state != null) {
            boolean log = !(this.service instanceof ServiceManager.NoOpService);
            if (log) {
               ServiceManager.logger.log(Level.SEVERE, "Service " + this.service + " has failed in the " + from + " state.", failure);
            }

            state.transitionService(this.service, from, Service.State.FAILED);
         }

      }
   }

   private static final class ServiceManagerState {
      final Monitor monitor = new Monitor();
      @GuardedBy("monitor")
      final SetMultimap<Service.State, Service> servicesByState = MultimapBuilder.enumKeys(Service.State.class).linkedHashSetValues().build();
      @GuardedBy("monitor")
      final Multiset<Service.State> states;
      @GuardedBy("monitor")
      final Map<Service, Stopwatch> startupTimers;
      @GuardedBy("monitor")
      boolean ready;
      @GuardedBy("monitor")
      boolean transitioned;
      final int numberOfServices;
      final Monitor.Guard awaitHealthGuard;
      final Monitor.Guard stoppedGuard;
      @GuardedBy("monitor")
      final List<ListenerCallQueue<ServiceManager.Listener>> listeners;

      ServiceManagerState(ImmutableCollection<Service> services) {
         this.states = this.servicesByState.keys();
         this.startupTimers = Maps.newIdentityHashMap();
         this.awaitHealthGuard = new ServiceManager.ServiceManagerState.AwaitHealthGuard();
         this.stoppedGuard = new ServiceManager.ServiceManagerState.StoppedGuard();
         this.listeners = Collections.synchronizedList(new ArrayList());
         this.numberOfServices = services.size();
         this.servicesByState.putAll(Service.State.NEW, services);
      }

      void tryStartTiming(Service service) {
         this.monitor.enter();

         try {
            Stopwatch stopwatch = (Stopwatch)this.startupTimers.get(service);
            if (stopwatch == null) {
               this.startupTimers.put(service, Stopwatch.createStarted());
            }
         } finally {
            this.monitor.leave();
         }

      }

      void markReady() {
         this.monitor.enter();

         try {
            if (this.transitioned) {
               List<Service> servicesInBadStates = Lists.newArrayList();
               Iterator i$ = this.servicesByState().values().iterator();

               while(i$.hasNext()) {
                  Service service = (Service)i$.next();
                  if (service.state() != Service.State.NEW) {
                     servicesInBadStates.add(service);
                  }
               }

               throw new IllegalArgumentException("Services started transitioning asynchronously before the ServiceManager was constructed: " + servicesInBadStates);
            }

            this.ready = true;
         } finally {
            this.monitor.leave();
         }

      }

      void addListener(ServiceManager.Listener listener, Executor executor) {
         Preconditions.checkNotNull(listener, "listener");
         Preconditions.checkNotNull(executor, "executor");
         this.monitor.enter();

         try {
            if (!this.stoppedGuard.isSatisfied()) {
               this.listeners.add(new ListenerCallQueue(listener, executor));
            }
         } finally {
            this.monitor.leave();
         }

      }

      void awaitHealthy() {
         this.monitor.enterWhenUninterruptibly(this.awaitHealthGuard);

         try {
            this.checkHealthy();
         } finally {
            this.monitor.leave();
         }

      }

      void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
         this.monitor.enter();

         try {
            if (!this.monitor.waitForUninterruptibly(this.awaitHealthGuard, timeout, unit)) {
               throw new TimeoutException("Timeout waiting for the services to become healthy. The following services have not started: " + Multimaps.filterKeys(this.servicesByState, Predicates.in(ImmutableSet.of(Service.State.NEW, Service.State.STARTING))));
            }

            this.checkHealthy();
         } finally {
            this.monitor.leave();
         }

      }

      void awaitStopped() {
         this.monitor.enterWhenUninterruptibly(this.stoppedGuard);
         this.monitor.leave();
      }

      void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
         this.monitor.enter();

         try {
            if (!this.monitor.waitForUninterruptibly(this.stoppedGuard, timeout, unit)) {
               throw new TimeoutException("Timeout waiting for the services to stop. The following services have not stopped: " + Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.in(EnumSet.of(Service.State.TERMINATED, Service.State.FAILED)))));
            }
         } finally {
            this.monitor.leave();
         }

      }

      ImmutableMultimap<Service.State, Service> servicesByState() {
         ImmutableSetMultimap.Builder<Service.State, Service> builder = ImmutableSetMultimap.builder();
         this.monitor.enter();

         try {
            Iterator i$ = this.servicesByState.entries().iterator();

            while(i$.hasNext()) {
               Entry<Service.State, Service> entry = (Entry)i$.next();
               if (!(entry.getValue() instanceof ServiceManager.NoOpService)) {
                  builder.put(entry);
               }
            }
         } finally {
            this.monitor.leave();
         }

         return builder.build();
      }

      ImmutableMap<Service, Long> startupTimes() {
         this.monitor.enter();

         ArrayList loadTimes;
         try {
            loadTimes = Lists.newArrayListWithCapacity(this.startupTimers.size());
            Iterator i$ = this.startupTimers.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<Service, Stopwatch> entry = (Entry)i$.next();
               Service service = (Service)entry.getKey();
               Stopwatch stopWatch = (Stopwatch)entry.getValue();
               if (!stopWatch.isRunning() && !(service instanceof ServiceManager.NoOpService)) {
                  loadTimes.add(Maps.immutableEntry(service, stopWatch.elapsed(TimeUnit.MILLISECONDS)));
               }
            }
         } finally {
            this.monitor.leave();
         }

         Collections.sort(loadTimes, Ordering.natural().onResultOf(new Function<Entry<Service, Long>, Long>() {
            public Long apply(Entry<Service, Long> input) {
               return (Long)input.getValue();
            }
         }));
         return ImmutableMap.copyOf((Iterable)loadTimes);
      }

      void transitionService(Service service, Service.State from, Service.State to) {
         Preconditions.checkNotNull(service);
         Preconditions.checkArgument(from != to);
         this.monitor.enter();

         try {
            this.transitioned = true;
            if (this.ready) {
               Preconditions.checkState(this.servicesByState.remove(from, service), "Service %s not at the expected location in the state map %s", service, from);
               Preconditions.checkState(this.servicesByState.put(to, service), "Service %s in the state map unexpectedly at %s", service, to);
               Stopwatch stopwatch = (Stopwatch)this.startupTimers.get(service);
               if (stopwatch == null) {
                  stopwatch = Stopwatch.createStarted();
                  this.startupTimers.put(service, stopwatch);
               }

               if (to.compareTo(Service.State.RUNNING) >= 0 && stopwatch.isRunning()) {
                  stopwatch.stop();
                  if (!(service instanceof ServiceManager.NoOpService)) {
                     ServiceManager.logger.log(Level.FINE, "Started {0} in {1}.", new Object[]{service, stopwatch});
                  }
               }

               if (to == Service.State.FAILED) {
                  this.fireFailedListeners(service);
               }

               if (this.states.count(Service.State.RUNNING) == this.numberOfServices) {
                  this.fireHealthyListeners();
               } else if (this.states.count(Service.State.TERMINATED) + this.states.count(Service.State.FAILED) == this.numberOfServices) {
                  this.fireStoppedListeners();
                  return;
               }

               return;
            }
         } finally {
            this.monitor.leave();
            this.executeListeners();
         }

      }

      @GuardedBy("monitor")
      void fireStoppedListeners() {
         ServiceManager.STOPPED_CALLBACK.enqueueOn(this.listeners);
      }

      @GuardedBy("monitor")
      void fireHealthyListeners() {
         ServiceManager.HEALTHY_CALLBACK.enqueueOn(this.listeners);
      }

      @GuardedBy("monitor")
      void fireFailedListeners(final Service service) {
         (new ListenerCallQueue.Callback<ServiceManager.Listener>("failed({service=" + service + "})") {
            void call(ServiceManager.Listener listener) {
               listener.failure(service);
            }
         }).enqueueOn(this.listeners);
      }

      void executeListeners() {
         Preconditions.checkState(!this.monitor.isOccupiedByCurrentThread(), "It is incorrect to execute listeners with the monitor held.");

         for(int i = 0; i < this.listeners.size(); ++i) {
            ((ListenerCallQueue)this.listeners.get(i)).execute();
         }

      }

      @GuardedBy("monitor")
      void checkHealthy() {
         if (this.states.count(Service.State.RUNNING) != this.numberOfServices) {
            IllegalStateException exception = new IllegalStateException("Expected to be healthy after starting. The following services are not running: " + Multimaps.filterKeys(this.servicesByState, Predicates.not(Predicates.equalTo(Service.State.RUNNING))));
            throw exception;
         }
      }

      final class StoppedGuard extends Monitor.Guard {
         StoppedGuard() {
            super(ServiceManagerState.this.monitor);
         }

         public boolean isSatisfied() {
            return ServiceManagerState.this.states.count(Service.State.TERMINATED) + ServiceManagerState.this.states.count(Service.State.FAILED) == ServiceManagerState.this.numberOfServices;
         }
      }

      final class AwaitHealthGuard extends Monitor.Guard {
         AwaitHealthGuard() {
            super(ServiceManagerState.this.monitor);
         }

         public boolean isSatisfied() {
            return ServiceManagerState.this.states.count(Service.State.RUNNING) == ServiceManagerState.this.numberOfServices || ServiceManagerState.this.states.contains(Service.State.STOPPING) || ServiceManagerState.this.states.contains(Service.State.TERMINATED) || ServiceManagerState.this.states.contains(Service.State.FAILED);
         }
      }
   }

   @Beta
   public abstract static class Listener {
      public void healthy() {
      }

      public void stopped() {
      }

      public void failure(Service service) {
      }
   }
}
