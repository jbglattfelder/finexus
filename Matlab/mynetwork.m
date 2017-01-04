%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Configuration
profilefile = 'top25-profile.csv'; % Orbis data
shareholderfile = 'top25-shareholders.csv';  % Orbis data
offset = 3; % Two header lines in data


%% Read profile
[no, name, bvdid, ctry, nace, oprev, ta, mcap, emp] = ...
  textread(profilefile,'%s %s %s %s %s %s %s %s %s','delimiter',';');
size = length(no);


%% Parse input
[no, name, bvdid, ctry, nace, oprev, ta, mcap, emp] = fParse(no, name, bvdid, ctry, nace, oprev, ta, mcap, emp, offset);


%% Network I: Create agents
ag = struct('bvdid',[],'name',[],'ctry',[],'nace',[],'oprev',[],'ta',[],'mcap',[],'emp',[]);
ag.name = name;
ag.bvdid = bvdid;
ag.ctry = ctry;
ag.nace = nace;
ag.oprev = oprev;
ag.ta = ta;
ag.mcap = mcap;
ag.emp = emp;


%% Read shareholders and network
[no, name, bvdid, shbvdid, shname, shctry, shnace, shoprev, shta, shemp, wdir, wtot] = ...
  textread(shareholderfile,'%s %s %s %s %s %s %s %s %s %s %s %s','delimiter',';');


%% Add shareholders as agents
[bvdid, shname, shbvdid, shctry, shnace, shoprev, shta, shemp, wdir, wtot] = fParseSh(bvdid, shname, shbvdid, shctry, shnace, shoprev, shta, shemp, wdir, wtot);
size = length(shbvdid);
% Loop through possible shareholders
for i = offset : size
    % Compare shareholder id with id of original firms
    ind = find(strcmp([ag.bvdid], shbvdid(i)));
    if isempty(ind) && ~strcmp('', shbvdid(i))
        % Not found in id list and not emppty: add
        if strcmp('', shbvdid(i))
            disp('')
        end
        ag.name = [ag.name; char(shname(i))];
        ag.bvdid = [ag.bvdid; shbvdid(i)];
        %disp(strcat(shname(i),' - ', shbvdid(i)))
        ag.ctry = [ag.ctry; shctry(i)];
        ag.nace = [ag.nace; shnace(i)];
        ag.oprev = [ag.oprev; shoprev(i)];
        ag.ta = [ag.ta; shta(i)];
        ag.emp = [ag.emp; shemp(i)];
        ag.mcap = [ag.mcap; 0];
    end
end


%% Network II: Create adjacency matrix
adsize = length(ag.bvdid);
ad = sparse(adsize, adsize);

for k = offset : size
    source = shbvdid(k);
    destination = bvdid(k);
    % Weight (Orbis specific)
    w = wdir(k);
    if (w == 0 && wtot(k) > 0)
        w = wtot(k);
    end
    % Indices
    i = find(strcmp([ag.bvdid], source));
    j = find(strcmp([ag.bvdid], destination));
    ad(i, j) = w;
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Indirect ownership/control

% The Network of Global Corporate Control; Stefania Vitali, James B. Glattfelder, Stefano Battiston; 2011
% Appendix S1 (Supporting Material)
% http://journals.plos.org/plosone/article?id=10.1371/journal.pone.0025995
I = eye(adsize);
ad_tilde = (I-ad)^-1 * ad; % Eq. (2) (also presented in lecture notes)
ii = ad_tilde * ag.oprev;
[m, index] = sort(ii, 'descend');

%% Top 10
disp('Top 10 Shareholders:')
ag.name(index(1:10))


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Save network to file (for Gephi)
% Agents
agents ='gephi-agents.csv';
fileID = fopen(agents,'w');
fprintf(fileID,'Id\tLabel\toprev\tii\n');
for i =1 : adsize
    fprintf(fileID,'%d\t%s\t%f\t%f\n', i, char(ag.name(i)), log(ag.oprev(i)+1), log(ii(i)+1));
end
fclose(fileID);


% Relations
rels = 'gephi-relations.csv';
fileID = fopen(rels,'w');
fprintf(fileID,'Source\tTarget\tType\tLabel\tWeight\n');
a = full(ad);
for i = 1 : adsize
    for j = 1: adsize
        if (ad(i,j) > 0)
            fprintf(fileID,'%d\t%d\t%s\t%s\t%f\n', i, j, 'Directed', 'Owns', a(i,j));
        end
    end
end
fclose(fileID);

