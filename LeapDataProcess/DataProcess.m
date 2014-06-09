% Plot the trace data of Leap C++ project, and process them.
% LI ZHEN, April 12th, 2014.
for i = 2:2
    ModelType = 2;                          % 2 types of 3D models
    AndroidFilePrefix = '0527';
    
    angle_1 = [66 88 0; -66 88 0; 0 88 66];
    angle_2 = [44 105.6 0; -44 105.6 0; 0 105.6 44];
    for r = 1:3
        angle_1(r, :) = angle_1(r, :) ./ sqrt(sum(angle_1(r, :) .^ 2));
        angle_2(r, :) = angle_2(r, :) ./ sqrt(sum(angle_2(r, :) .^ 2));
    end
    angle = cat(3, angle_1, angle_2);
    
    stdEdge = [66 * sqrt(2), 66 * sqrt(2), 132, 110; 44 * sqrt(2), 44 * sqrt(2), 88, 114.4];
    
    dirName = '../LeapTest/LeapTest';
    fileNo = sprintf('3%d0%d.csv', ModelType, i);
    fileName = sprintf('%s/FingerMove%s', dirName, fileNo);
    mat = csvread(fileName, 1, 0, [1 0 1 1]);
    
    % Get current time (ms)
    % Magic Time: 1401178000000ms
    timeBase = 1401178000000;
    currTime = mat(1) * 1000 + mat(2) - timeBase;
    
    % timestamp(us) id1 x1 y1 z1 id2 x2 y2 z2 id3 x3 y3 z3 edge1 edge2 edge3
    % edge1:E12, edge2:E13, edge3:E23
    format long;
    mat = csvread(fileName, 3, 0);
    
    fprintf('%d:\n', i);
    [row col] = size(mat);
    
    plotOriginal = 1;
    plotCenter = 1;
    
    if plotOriginal
        for r = 1:row
            plot3(mat(r, 3), mat(r, 4), mat(r, 5), '.r');
            grid on;
            hold on;
            if mat(r, 6) == 1
                plot3(mat(r, 7), mat(r, 8), mat(r, 9), '.b');
                hold on;
                if mat(r, 10) == 2
                    plot3(mat(r, 11), mat(r, 12), mat(r, 13), '.c');
                    hold on;
                end
            end
        end
    end
    
    if plotCenter
        timeArray = mat(:, 1) / 1000.0 + currTime;
        
        % Get sorted points: p1, p2, p3 in order
        [newTime newP1 newP2 newP3] = AdjustPoints(timeArray, mat(:, 2:13));
        adjFileName = sprintf('%s/AdjustPoints%s', dirName, fileNo);
        fid = fopen(adjFileName, 'w');
        fprintf(fid, 'time(ms), id1, x1(mm), y1, z1, id2, x2, y2, z2, id3, x3, y3, z3\n');
        fclose(fid);
        [adjR adjC] = size(newP1);
        dlmwrite(adjFileName, [newTime zeros(adjR, 1) newP1 ones(adjR, 1) newP2 2*ones(adjR, 1) newP3],...,
            'precision', 11, '-append');
        
        % Get Center Point of the device
        centerP = GetCenterPos(newTime, newP1, newP2, newP3, ...,
            stdEdge(ModelType, :), angle(:, :, ModelType));
        
        plot3(centerP(:, 2), centerP(:, 3), centerP(:, 4), '*g');
        grid on;
        hold on;
        
        [pRow pCol] = size(centerP);
        interval = 0.5;                 % interpolate with 0.5ms
        interTime = centerP(1, 1): interval: centerP(pRow, 1);
        interCPos = interp1(centerP(:, 1), centerP(:, 2:4), interTime, 'spline');
        interP1 = interp1(centerP(:, 1), newP1, interTime, 'spline');
        interP2 = interp1(centerP(:, 1), newP2, interTime, 'spline');
        interP3 = interp1(centerP(:, 1), newP3, interTime, 'spline');
        interP4 = (interP2 + interP3) ./ 2;
        plot3(interCPos(:, 1), interCPos(:, 2), interCPos(:, 3), '.m');
        [iRow iCol] = size(interCPos);
        
        % Get acceleration. convert mm/ms2 to m/s2
        accCenter = diff(interCPos, 2) / (interval ^ 2) * 1000;
        [aRow aCol] = size(accCenter);
        
        % Get Direction X, Y, Z
        dirX = interP2 - interP4;
        dirY = interP1 - interP4;
        dirZ = interCPos - interP4;
        % Normalization
        zeroP = zeros(aRow, 3);
        dirXLen = GetDistance(dirX(2:aRow+1, :), zeroP);
        dirYLen = GetDistance(dirY(2:aRow+1, :), zeroP);
        dirZLen = GetDistance(dirZ(2:aRow+1, :), zeroP);
        dirXNorm = dirX(2:aRow+1, :) ./ [dirXLen, dirXLen, dirXLen];
        dirYNorm = dirY(2:aRow+1, :) ./ [dirYLen, dirYLen, dirYLen];
        dirZNorm = dirZ(2:aRow+1, :) ./ [dirZLen, dirZLen, dirZLen];
        
        % Convert the center point acc. to direction X, Y, Z of the device        
        accX = sum(accCenter .* dirXNorm, 2);
        accY = sum(accCenter .* dirYNorm, 2);
        accZ = sum(accCenter .* dirZNorm, 2);
        
        output = zeros(iRow, 7);
        output(:, 1) = interTime;
        output(:, 2:4) = interCPos;
        output(1:aRow, 5:7) = [accX accY accZ];
        
        outFileName = sprintf('%s/CenterPos%s', dirName, fileNo);
        fid = fopen(outFileName, 'w');
        fprintf(fid, 'time(ms), x(mm), y, z, ax(m/s2), ay, az\n');
        fclose(fid);
        dlmwrite(outFileName, output, 'precision', 11, '-append');
        
        % Load Android App Data
        AndroidFileName = sprintf('%s-%d (%d).csv', AndroidFilePrefix, ModelType, i);
        androidMat = csvread(AndroidFileName, 1, 0, [1 0 1 0]);
        androidTime = androidMat(1) - timeBase;
        
        androidMat = csvread(AndroidFileName, 3, 0);
        androidTime = androidMat(:, 1) + androidTime;
        
        figure;
        plot(interTime(2:aRow+1) - interTime(2), accX', 'g');
        hold on;
        plot(androidTime' - androidTime(1), androidMat(:, 2)', 'r');
        
        figure;
        plot(interTime(2:aRow+1) - interTime(2), accY', 'g');
        hold on;
        plot(androidTime' - androidTime(1), androidMat(:, 3)', 'r');
        
        figure;
        plot(interTime(2:aRow+1) - interTime(2), accZ', 'g');
        hold on;
        plot(androidTime' - androidTime(1), androidMat(:, 4)', 'r');
    end
end
